package com.cyberspectraa.cyberraces.client;

import net.minecraft.client.Minecraft;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void openCharacterCreator() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new CharacterCreatorScreen());
    }

    public static void closeCharacterCreator() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CharacterCreatorScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void openRaceEvolution(String baseRaceId) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!(minecraft.screen instanceof RaceEvolutionScreen)) {
            minecraft.setScreen(new RaceEvolutionScreen(baseRaceId));
        }
    }

    public static void closeRaceEvolution() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.screen instanceof RaceEvolutionScreen) {
            minecraft.setScreen(null);
        }
    }
}
