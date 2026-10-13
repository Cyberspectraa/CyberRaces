package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.network.packet.RaceSensePacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
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

        // Client-only line boxes keep the display quiet and readable,
        // even if valuable blocks are behind rock or enclosed in chests.
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        RenderSystem.disableDepthTest();
        for (BlockPos pos : ORES)
            outline(pose, lines, pos, 1.00F, 0.81F, 0.18F);
        for (BlockPos pos : CONTAINERS)
            outline(pose, lines, pos, 0.20F, 0.86F, 0.85F);
        buffers.endBatch(RenderType.lines());
        RenderSystem.enableDepthTest();
        pose.popPose();
    }

    private static void outline(PoseStack pose, VertexConsumer lines, BlockPos pos,
                                float r, float g, float b) {
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        LevelRenderer.renderLineBox(pose, lines,
                x + 0.015D, y + 0.015D, z + 0.015D,
                x + 0.985D, y + 0.985D, z + 0.985D,
                r, g, b, 0.95F);
    }
}
