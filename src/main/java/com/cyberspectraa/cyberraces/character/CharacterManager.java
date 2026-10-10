package com.cyberspectraa.cyberraces.character;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CloseCharacterCreatorPacket;
import com.cyberspectraa.cyberraces.network.packet.OpenCharacterCreatorPacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class CharacterManager {
    public static final String ROOT_KEY = "CyberRaces";
    public static final String CREATED_KEY = "CharacterCreated";
    public static final String APPEARANCE_KEY = "Appearance";

    private static final String RACE_KEY = "Race";

    private CharacterManager() {
    }

    public static void handleLogin(ServerPlayer player) {
        migrateLegacy(player);

        if (isCharacterCreated(player)) {
            // An interrupted first-arrival intro must reclaim the hidden player
            // before the default creator hold releases invisibility.
            if (introHook(player, "resumeIfPending")) CreationHoldManager.forget(player);
            else CreationHoldManager.release(player);
            return;
        }

        setCharacterCreated(player, false);
        CreationHoldManager.enter(player);
        CyberRacesNetwork.sendToPlayer(player, new OpenCharacterCreatorPacket());
    }

    public static boolean isCharacterCreated(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);
        return root.contains(CREATED_KEY) && root.getBoolean(CREATED_KEY);
    }

    public static CharacterAppearance getAppearance(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);
        if (!root.contains(APPEARANCE_KEY)) {
            return CharacterAppearance.defaults();
        }
        return CharacterAppearance.load(root.getCompound(APPEARANCE_KEY));
    }

    public static boolean completeCharacter(
        ServerPlayer player,
        Race race,
        CharacterAppearance appearance
    ) {
        if (isCharacterCreated(player)) {
            return false;
        }

        saveAppearance(player, appearance);
        setCharacterCreated(player, true);
        RaceManager.setRace(player, race);
        // Close character creation before the server sends the cinematic
        // screen. Keep invisibility until the summoning reveal, not creation.
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseCharacterCreatorPacket());
        if (introHook(player, "beginSummoning")) CreationHoldManager.forget(player);
        else CreationHoldManager.release(player);
        return true;
    }

    public static void forceComplete(
        ServerPlayer player,
        Race race,
        CharacterAppearance appearance
    ) {
        saveAppearance(player, appearance);
        setCharacterCreated(player, true);
        RaceManager.setRace(player, race);
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseCharacterCreatorPacket());
        if (introHook(player, "beginSummoning")) CreationHoldManager.forget(player);
        else CreationHoldManager.release(player);
    }

    public static void resetCharacter(ServerPlayer player) {
        RaceManager.clearRace(player);

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putBoolean(CREATED_KEY, false);
        root.remove(APPEARANCE_KEY);
        persistent.put(ROOT_KEY, root);

        CreationHoldManager.enter(player);
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new OpenCharacterCreatorPacket());
    }

    public static void tickCreationHold(ServerPlayer player) {
        if (!isCharacterCreated(player)) {
            CreationHoldManager.tick(player);
        }
    }

    public static void onLogout(ServerPlayer player) {
        CreationHoldManager.forget(player);
    }

    private static boolean introHook(ServerPlayer player, String methodName) {
        // No hard class dependency: CyberRaces continues working without
        // CyberNpc installed, while the combined pack gets a seamless handoff.
        if (!ModList.get().isLoaded("cybernpc")) return false;
        try {
            Class<?> service = Class.forName(
                    "com.cyberspectraa.cybernpc.intro.CyberIntroService");
            Method method = service.getMethod(methodName, ServerPlayer.class);
            return Boolean.TRUE.equals(method.invoke(null, player));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static void migrateLegacy(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_KEY)) {
            return;
        }

        CompoundTag root = persistent.getCompound(ROOT_KEY);

        if (!root.contains(CREATED_KEY) && root.contains(RACE_KEY)) {
            root.putBoolean(CREATED_KEY, true);
            if (!root.contains(APPEARANCE_KEY)) {
                root.put(APPEARANCE_KEY, CharacterAppearance.defaults().save());
            } else {
                CharacterAppearance cleaned = CharacterAppearance.load(root.getCompound(APPEARANCE_KEY));
                root.put(APPEARANCE_KEY, cleaned.save());
            }
            persistent.put(ROOT_KEY, root);
        }
    }

    private static void saveAppearance(ServerPlayer player, CharacterAppearance appearance) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.put(APPEARANCE_KEY, appearance.save());
        persistent.put(ROOT_KEY, root);
    }

    private static void setCharacterCreated(ServerPlayer player, boolean created) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putBoolean(CREATED_KEY, created);
        persistent.put(ROOT_KEY, root);
    }
}
