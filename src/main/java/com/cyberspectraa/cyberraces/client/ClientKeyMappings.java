package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
    modid = CyberRaces.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT
)
public final class ClientKeyMappings {
    // Defer KeyMapping construction until Forge's registration phase.
    // A static initializer here can mutate Forge's mapping lookup while
    // another mod is registering keys, causing ConcurrentModificationException.
    // Keep the public field for existing client callers.
    public static volatile KeyMapping RACIAL_ABILITY;

    private ClientKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        KeyMapping ability = new KeyMapping(
            "key.cyberraces.racial_ability",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.cyberraces"
        );
        event.register(ability);
        RACIAL_ABILITY = ability;
    }
}
