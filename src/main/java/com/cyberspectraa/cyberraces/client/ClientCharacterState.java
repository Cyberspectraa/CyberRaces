package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ClientCharacterState {
    private static final Map<UUID, VisualCharacter> CHARACTERS = new HashMap<>();

    private static Race previewRace;
    private static CharacterAppearance previewAppearance = CharacterAppearance.defaults();

    private ClientCharacterState() {
    }

    public static void apply(
        UUID playerId,
        boolean created,
        String raceId,
        String evolutionId,
        int featureStyle,
        int featureColor,
        int earHeight,
        int earSpread,
        int earTilt,
        int bodySourceColor,
        int bodyTargetColor,
        int bodyTolerance
    ) {
        if (!created) {
            CHARACTERS.remove(playerId);
            RaceSkinOverlayManager.invalidate(playerId);
            return;
        }

        Race.byId(raceId).ifPresent(race -> {
            CharacterAppearance appearance = new CharacterAppearance(
                featureStyle,
                featureColor,
                earHeight,
                earSpread,
                earTilt,
                bodySourceColor,
                bodyTargetColor,
                bodyTolerance
            );

            VisualCharacter previous = CHARACTERS.put(
                playerId,
                new VisualCharacter(
                    race,
                    evolutionId == null ? "" : evolutionId,
                    appearance
                )
            );

            if (previous == null
                || previous.race() != race
                || !previous.evolutionId().equals(
                    evolutionId == null ? "" : evolutionId
                )
                || !previous.appearance().equals(appearance)) {
                RaceSkinOverlayManager.invalidate(playerId);
            }
        });
    }

    public static Optional<VisualCharacter> resolve(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (previewRace != null
            && minecraft.player != null
            && player.getUUID().equals(minecraft.player.getUUID())) {
            return Optional.of(
                new VisualCharacter(previewRace, "", previewAppearance)
            );
        }

        return resolve(player.getUUID());
    }

    public static Optional<VisualCharacter> resolve(UUID entityId) {
        if (entityId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(CHARACTERS.get(entityId));
    }

    public static void setPreview(Race race, CharacterAppearance appearance) {
        if (previewRace != race || !previewAppearance.equals(appearance)) {
            previewRace = race;
            previewAppearance = appearance;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                RaceSkinOverlayManager.invalidate(minecraft.player.getUUID());
            }
        }
    }

    public static void clearPreview() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            RaceSkinOverlayManager.invalidate(minecraft.player.getUUID());
        }
        previewRace = null;
        previewAppearance = CharacterAppearance.defaults();
    }

    public static boolean isPreviewing(Race race, AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        return previewRace == race
            && minecraft.player != null
            && player.getUUID().equals(minecraft.player.getUUID());
    }

    public static void clearAll() {
        CHARACTERS.clear();
        clearPreview();
        RaceSkinOverlayManager.clearAll();
    }

    public record VisualCharacter(
        Race race,
        String evolutionId,
        CharacterAppearance appearance
    ) {
        public int featureStyle() {
            return appearance.featureStyle();
        }

        public int featureColor() {
            return appearance.featureColor();
        }

        public int earHeight() {
            return appearance.earHeight();
        }

        public int earSpread() {
            return appearance.earSpread();
        }

        public int earTilt() {
            return appearance.earTilt();
        }
    }
}
