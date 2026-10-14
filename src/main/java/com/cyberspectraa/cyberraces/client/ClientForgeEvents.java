package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.event.FairyHoverPhysics;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.RacialAbilityPacket;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
    modid = CyberRaces.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    value = Dist.CLIENT
)
public final class ClientForgeEvents {
    private static LocalPlayer arachnidWallHoldPlayer;

    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void hideInvisiblePlayers(RenderPlayerEvent.Pre event) {
        // Cancelling the complete renderer hides held items, armor, race
        // cosmetics, and nameplates along with the body during creation.
        if (event.getEntity().isInvisible()) event.setCanceled(true);
    }

    private static void releaseArachnidHold() {
        if (arachnidWallHoldPlayer != null) {
            arachnidWallHoldPlayer.setNoGravity(false);
            arachnidWallHoldPlayer = null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            releaseArachnidHold();
            ClientAbilityState.disableFairyHover();
            return;
        }
        if (arachnidWallHoldPlayer != null
                && arachnidWallHoldPlayer != minecraft.player) {
            releaseArachnidHold();
        }

        Race race =
            ClientCharacterState.resolve(minecraft.player.getUUID())
                .map(ClientCharacterState.VisualCharacter::race)
                .orElse(null);

        LocalPlayer player = minecraft.player;
        boolean arachnidOnWall = race == Race.ARACHNID
                && (player.horizontalCollision
                    || (player.isShiftKeyDown() && player.level()
                        .getBlockCollisions(player, player.getBoundingBox()
                            .inflate(0.09D, -0.12D, 0.09D))
                        .iterator().hasNext()))
                && !player.isPassenger() && !player.isInWaterOrBubble()
                && !player.isFallFlying() && !player.getAbilities().flying
                && !player.isSpectator();
        if (arachnidOnWall && player.isShiftKeyDown()) {
            // A true ladder-style hold: no vertical velocity OR gravity.
            if (!player.isNoGravity()) {
                player.setNoGravity(true);
                arachnidWallHoldPlayer = player;
            }
            var motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.0D, motion.z);
            player.fallDistance = 0.0F;
        } else {
            releaseArachnidHold();
            if (arachnidOnWall) {
                var motion = player.getDeltaMovement();
                player.setDeltaMovement(
                        motion.x, Math.max(motion.y, 0.20D), motion.z);
                player.fallDistance = 0.0F;
            }
        }

        if (race != Race.FAIRY) {
            ClientAbilityState.disableFairyHover();
        } else if (ClientAbilityState.isFairyHoverEnabled()) {
            FairyHoverPhysics.apply(minecraft.player);
        }

        if (minecraft.screen != null) {
            return;
        }

        // The mapping exists only after RegisterKeyMappingsEvent; do not
        // assume class loading and registration finish at the same time.
        KeyMapping racialAbility = ClientKeyMappings.RACIAL_ABILITY;
        if (racialAbility == null) return;

        while (racialAbility.consumeClick()) {
            if (race == null) {
                minecraft.player.displayClientMessage(
                    Component.literal(
                        "Choose a race before using a racial ability."
                    ),
                    true
                );
                continue;
            }

            // Slimes now bounce naturally; V is no longer an ability.
            if (race == Race.SLIME) continue;

            boolean toggleState = false;

            if (race == Race.FAIRY) {
                toggleState =
                    ClientAbilityState.toggleFairyHover();
            }

            CyberRacesNetwork.sendToServer(
                new RacialAbilityPacket(toggleState)
            );
        }
    }
}
