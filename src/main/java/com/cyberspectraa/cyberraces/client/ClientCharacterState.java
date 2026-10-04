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
        int featureStyle,
        int featureColor,
        int earHeight,
        int earSpread,
        int earTilt
    ) {
        if (!created) {
            CHARACTERS.remove(playerId);
            return;
        }

        Race.byId(raceId).ifPresent(race ->
            CHARACTERS.put(
                playerId,
                new VisualCharacter(
                    race,
                    new CharacterAppearance(featureStyle, featureColor, earHeight, earSpread, earTilt)
                )
            )
        );
    }

    public static Optional<VisualCharacter> resolve(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (previewRace != null
            && minecraft.player != null
            && player.getUUID().equals(minecraft.player.getUUID())) {
            return Optional.of(new VisualCharacter(previewRace, previewAppearance));
        }

        return Optional.ofNullable(CHARACTERS.get(player.getUUID()));
    }

    public static void setPreview(Race race, CharacterAppearance appearance) {
        previewRace = race;
        previewAppearance = appearance;
    }

    public static void clearPreview() {
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
    }

    public record VisualCharacter(Race race, CharacterAppearance appearance) {
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
