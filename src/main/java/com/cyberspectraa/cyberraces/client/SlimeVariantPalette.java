package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.ModList;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * Visual-only slime forms. A fixed numeric slot is saved with the character;
 * additions cannot move earlier slots when optional mods come and go.
 * Mods/resource packs can contribute assets/<namespace>/cyberraces/slime_variants/*.json
 * (slot, name, color, optional requires_mod) from slot 12 onwards.
 */
public final class SlimeVariantPalette {
    private record Variant(String name, int rgb) {}

    private static ResourceManager cachedManager;
    private static Variant[] cached;
    private static final int EXTENSION_START = 12;

    private SlimeVariantPalette() {}

    public static String name(int slot) {
        Variant item = variants()[slotIndex(slot)];
        return item == null ? "Slime" : item.name();
    }

    public static int color(int slot) {
        Variant item = variants()[slotIndex(slot)];
        return item == null ? 0x73D36A : item.rgb();
    }

    public static int nextAvailable(int current) {
        Variant[] options = variants();
        for (int offset = 1; offset <= options.length; offset++) {
            int next = (slotIndex(current) + offset) % options.length;
            if (options[next] != null) return next;
        }
        return 0;
    }

    public static boolean hasAlternatives() {
        return variants()[1] != null;
    }

    private static int slotIndex(int slot) {
        return Math.max(0, Math.min(CharacterAppearance.FEATURE_STYLE_COUNT - 1, slot));
    }

    private static synchronized Variant[] variants() {
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        if (resources == cachedManager && cached != null) return cached;

        Variant[] loaded = new Variant[CharacterAppearance.FEATURE_STYLE_COUNT];
        loaded[0] = new Variant("Slime", 0x73D36A);
        loaded[1] = new Variant("Magma Slime", 0xF47C37);
        loaded[2] = new Variant("Metal Slime", 0xA8C5CF);
        loaded[3] = new Variant("Acid Slime", 0xBAED37);
        loaded[4] = new Variant("Crystal Slime", 0x94E9F9);
        loaded[5] = new Variant("Frost Slime", 0x79C9F2);
        loaded[6] = new Variant("Void Slime", 0x8360B1);
        loaded[7] = new Variant("Royal Slime", 0xF4D16B);

        // Tinkers' Construct is optional. These are inspired by its named
        // slime types, not copied mob models or textures.
        if (ModList.get().isLoaded("tconstruct")) {
            loaded[8] = new Variant("Sky Slime", 0x69C9F0);
            loaded[9] = new Variant("Earth Slime", 0x74AC71);
            loaded[10] = new Variant("Ender Slime", 0xA96CDD);
            loaded[11] = new Variant("Ichor Slime", 0xEDAD5F);
        }

        // Resource locations are sorted so conflicting duplicate slots have
        // a predictable winner across clients using the same resource packs.
        Map<ResourceLocation, Resource> sources =
                new TreeMap<>((a, b) -> a.toString().compareTo(b.toString()));
        sources.putAll(resources.listResources("cyberraces/slime_variants",
                key -> key.getPath().endsWith(".json")));
        for (Map.Entry<ResourceLocation, Resource> entry : sources.entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                int slot = json.get("slot").getAsInt();
                if (slot < EXTENSION_START || slot >= loaded.length
                        || loaded[slot] != null) continue;

                String requiredMod = json.has("requires_mod")
                        ? json.get("requires_mod").getAsString().trim() : "";
                if (!requiredMod.isEmpty() && !ModList.get().isLoaded(requiredMod)) continue;
                String label = json.get("name").getAsString().trim();
                if (label.isEmpty() || label.length() > 40) continue;
                String hex = json.get("color").getAsString().replace("#", "");
                if (!hex.matches("[0-9A-Fa-f]{6}")) continue;
                loaded[slot] = new Variant(label, Integer.parseInt(hex, 16));
            } catch (Exception ex) {
                CyberRaces.LOGGER.warn("Ignored invalid slime variant at {}: {}",
                        entry.getKey(), ex.getMessage());
            }
        }

        cachedManager = resources;
        cached = loaded;
        return loaded;
    }
}
