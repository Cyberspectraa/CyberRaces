package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.network.packet.RaceSensePacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Per-viewer temporary highlights; nothing permanent is written to entities or worlds. */
@Mod.EventBusSubscriber(modid = CyberRaces.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientRaceSenseState {
    private static List<BlockPos> ORES = List.of();
    private static List<BlockPos> CONTAINERS = List.of();
    private static final Map<UUID, Entity> GLOWING = new HashMap<>();
    private static long treasureEndsAt;
    private static long creaturesEndAt;
    private static String dimension = "";

    // Eight box corners are connected by exactly twelve independently
    // rendered strips. Each edge is a camera-facing quad, avoiding the
    // special Minecraft line shader that dropped edges on some GPUs.
    private static final int[][] BOX_EDGES = {
        {0,1}, {1,2}, {2,3}, {3,0},
        {4,5}, {5,6}, {6,7}, {7,4},
        {0,4}, {1,5}, {2,6}, {3,7}
    };
    private static final double EDGE_HALF_WIDTH = 0.012D;

    private ClientRaceSenseState() {}

    public static void accept(RaceSensePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        String current = mc.level.dimension().location().toString();
        if (!dimension.equals(current)) clear();
        dimension = current;

        long tick = mc.level.getGameTime();
        if (packet.mode() == RaceSensePacket.TREASURE) {
            ORES = List.copyOf(packet.ores());
            CONTAINERS = List.copyOf(packet.containers());
            treasureEndsAt = tick + 8 * 20;
        } else if (packet.mode() == RaceSensePacket.CREATURES) {
            clearGlows();
            creaturesEndAt = tick + 7 * 20;
            int n = 0;
            for (int id : packet.creatures()) {
                Entity entity = mc.level.getEntity(id);
                if (!(entity instanceof LivingEntity)
                        || entity == mc.player || entity.isCurrentlyGlowing()) continue;
                if (n++ >= 16) break;
                entity.setGlowingTag(true);
                GLOWING.put(entity.getUUID(), entity);
            }
        }
    }

    private static void clearGlows() {
        for (Entity entity : GLOWING.values())
            if (!entity.isRemoved()) entity.setGlowingTag(false);
        GLOWING.clear();
    }

    private static void clear() {
        clearGlows();
        ORES = List.of();
        CONTAINERS = List.of();
        treasureEndsAt = 0;
        creaturesEndAt = 0;
        dimension = "";
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clear();
            return;
        }
        if (!dimension.isEmpty()
                && !dimension.equals(mc.level.dimension().location().toString())) {
            clear();
            return;
        }

        long now = mc.level.getGameTime();
        Race race = ClientCharacterState.resolve(mc.player)
                .map(ClientCharacterState.VisualCharacter::race).orElse(null);
        if (race != Race.GOBLIN || now >= treasureEndsAt) {
            ORES = List.of();
            CONTAINERS = List.of();
            treasureEndsAt = 0;
        }
        if (race != Race.DOGFOLK || now >= creaturesEndAt) {
            clearGlows();
            creaturesEndAt = 0;
        } else {
            // Entity data can be refreshed by other packets; maintain the
            // temporary glow while the Dogfolk sensing window is still active.
            GLOWING.values().removeIf(Entity::isRemoved);
            for (Entity entity : GLOWING.values()) entity.setGlowingTag(true);
        }
    }

    @SubscribeEvent
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || (ORES.isEmpty() && CONTAINERS.isEmpty())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || mc.level.getGameTime() >= treasureEndsAt) return;

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        /*
         * The Minecraft lines shader expands line segments using their
         * normals and gl_VertexID. In immediate mode that produced missing
         * edges on some systems, despite successfully disabling depth.
         *
         * Render all twelve edges as slender camera-facing QUADS using the
         * ordinary position/color shader. This has no special line-normal
         * requirements, and explicitly draws the entire outline through
         * the wall. The renderer never writes to the depth buffer.
         */
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (BlockPos pos : ORES)
            outline(pose, buffer, pos, camera, 1.00F, 0.81F, 0.18F);
        for (BlockPos pos : CONTAINERS)
            outline(pose, buffer, pos, camera, 0.20F, 0.86F, 0.85F);

        try {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            pose.popPose();
        }
    }

    private static void outline(PoseStack pose, BufferBuilder buffer, BlockPos pos,
                                Vec3 camera, float r, float g, float b) {
        double x0 = pos.getX() + 0.01D;
        double y0 = pos.getY() + 0.01D;
        double z0 = pos.getZ() + 0.01D;
        double x1 = pos.getX() + 0.99D;
        double y1 = pos.getY() + 0.99D;
        double z1 = pos.getZ() + 0.99D;

        Vec3[] vertices = {
            new Vec3(x0,y0,z0), new Vec3(x1,y0,z0),
            new Vec3(x1,y0,z1), new Vec3(x0,y0,z1),
            new Vec3(x0,y1,z0), new Vec3(x1,y1,z0),
            new Vec3(x1,y1,z1), new Vec3(x0,y1,z1)
        };

        Matrix4f transform = pose.last().pose();
        for (int[] edge : BOX_EDGES) {
            Vec3 a = vertices[edge[0]];
            Vec3 bPos = vertices[edge[1]];
            Vec3 along = bPos.subtract(a).normalize();
            Vec3 toCamera = camera.subtract(a.add(bPos).scale(0.5D));
            Vec3 side = along.cross(toCamera);

            // Avoid degenerate quads when looking straight along an edge.
            if (side.lengthSqr() < 1.0E-8D)
                side = along.cross(new Vec3(0,1,0));
            if (side.lengthSqr() < 1.0E-8D)
                side = along.cross(new Vec3(1,0,0));
            side = side.normalize().scale(EDGE_HALF_WIDTH);

            vertex(buffer, transform, a.add(side), r, g, b);
            vertex(buffer, transform, bPos.add(side), r, g, b);
            vertex(buffer, transform, bPos.subtract(side), r, g, b);
            vertex(buffer, transform, a.subtract(side), r, g, b);
        }
    }

    private static void vertex(BufferBuilder buffer, Matrix4f transform,
                               Vec3 point, float r, float g, float b) {
        buffer.vertex(transform, (float) point.x, (float) point.y, (float) point.z)
                .color(r, g, b, 1.0F).endVertex();
    }
}
