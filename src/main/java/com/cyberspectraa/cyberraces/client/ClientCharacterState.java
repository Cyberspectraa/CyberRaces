package com.cyberspectraa.cyberraces.client;

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
    private static int previewFeatureStyle;
    private static int previewFeatureColor;

    private ClientCharacterState() {
    }

    public static void apply(UUID playerId, boolean created, String raceId, int featureStyle, int featureColor) {
        if (!created) {
            CHARACTERS.remove(playerId);
            return;
        }

        Race.byId(raceId).ifPresent(race ->
            CHARACTERS.put(
                playerId,
                new VisualCharacter(race, clampFeature(featureStyle), clampColor(featureColor))
            )
        );
    }

    public static Optional<VisualCharacter> resolve(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (previewRace != null && minecraft.player != null && player.getUUID().equals(minecraft.player.getUUID())) {
            return Optional.of(new VisualCharacter(previewRace, previewFeatureStyle, previewFeatureColor));
        }

        return Optional.ofNullable(CHARACTERS.get(player.getUUID()));
    }

    public static void setPreview(Race race, int featureStyle, int featureColor) {
        previewRace = race;
        previewFeatureStyle = clampFeature(featureStyle);
        previewFeatureColor = clampColor(featureColor);
    }

    public static void clearPreview() {
        previewRace = null;
        previewFeatureStyle = 0;
        previewFeatureColor = 0;
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
    }

    private static int clampFeature(int value) {
        return Math.max(0, Math.min(2, value));
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(CharacterColourCount.VALUE - 1, value));
    }

    private static final class CharacterColourCount {
        private static final int VALUE = 11;
    }

    public record VisualCharacter(Race race, int featureStyle, int featureColor) {
    }
}
