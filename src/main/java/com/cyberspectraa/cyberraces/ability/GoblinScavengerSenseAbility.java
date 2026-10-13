package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.RaceSensePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** V: Treasure Instinct. Nearby valuable ore and stocked containers get
 * a temporary, client-only outline, with no world edits or particle spam. */
public final class GoblinScavengerSenseAbility {
    public static final int ACTIVE_TICKS = 8 * 20;
    public static final int COOLDOWN_TICKS = 30 * 20;
    private static final int RADIUS = 14;
    private static final int VERTICAL_RADIUS = 7;
    private static final String READY_TICK_KEY = "GoblinScavengerReadyTick";

    private GoblinScavengerSenseAbility() {}

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        CompoundTag root = player.getPersistentData()
                .getCompound(CharacterManager.ROOT_KEY);
        long ready = root.getLong(READY_TICK_KEY);
        if (ready > now) {
            player.displayClientMessage(Component.literal(
                    "Treasure Instinct: " + ((ready - now + 19L) / 20L)
                            + "s cooldown"), true);
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) return;
        BlockPos origin = player.blockPosition();
        List<BlockPos> ores = new ArrayList<>();
        List<BlockPos> containers = new ArrayList<>();

        // One bounded scan on V, not repeated chunk/block-entity scans.
        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-RADIUS, -VERTICAL_RADIUS, -RADIUS),
                origin.offset(RADIUS, VERTICAL_RADIUS, RADIUS))) {
            if (!level.hasChunkAt(cursor)
                    || cursor.distSqr(origin) > RADIUS * RADIUS + 2) continue;

            BlockState state = level.getBlockState(cursor);
            if (state.is(Tags.Blocks.ORES) || state.is(Blocks.ANCIENT_DEBRIS)) {
                ores.add(cursor.immutable());
            } else if (state.hasBlockEntity()) {
                BlockEntity be = level.getBlockEntity(cursor);
                if (be instanceof Container container && !container.isEmpty())
                    containers.add(cursor.immutable());
            }
        }

        Comparator<BlockPos> nearby = Comparator.comparingDouble(
                pos -> pos.distSqr(origin));
        ores.sort(nearby);
        containers.sort(nearby);
        List<BlockPos> foundOres = ores.stream().limit(16).toList();
        List<BlockPos> foundContainers = containers.stream().limit(8).toList();
        CyberRacesNetwork.sendToPlayer(player,
                RaceSensePacket.treasure(foundOres, foundContainers));

        root.putLong(READY_TICK_KEY, now + COOLDOWN_TICKS);
        player.getPersistentData().put(CharacterManager.ROOT_KEY, root);
        player.displayClientMessage(Component.literal(
                "Treasure Instinct: " + foundOres.size() + " ore deposits, "
                        + foundContainers.size() + " stocked caches (8s)"), true);
    }

    // Legacy lifecycle hooks retained for RacialPassiveEvents compatibility.
    public static void tick(ServerPlayer player) {}
    public static void cleanup(ServerPlayer player) {}
}
