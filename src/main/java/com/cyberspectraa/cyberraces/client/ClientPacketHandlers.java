package com.cyberspectraa.cyberraces.client;

import net.minecraft.client.Minecraft;
import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void openCharacterCreator() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new CharacterCreatorScreen());
    }

    public static void openAppearanceMirror(String raceId, String evolutionId,
            CharacterAppearance appearance) {
        Race.byId(raceId).ifPresent(race ->
            Minecraft.getInstance().setScreen(
                new CharacterCreatorScreen(race, evolutionId, appearance)));
    }

    public static void closeCharacterCreator() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CharacterCreatorScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void openRaceEvolution(String proposedEvolutionId) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!(minecraft.screen instanceof RaceEvolutionScreen)) {
            minecraft.setScreen(new RaceEvolutionScreen(proposedEvolutionId));
        }
    }

    public static void closeRaceEvolution() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.screen instanceof RaceEvolutionScreen) {
            minecraft.setScreen(null);
        }
    }
}
