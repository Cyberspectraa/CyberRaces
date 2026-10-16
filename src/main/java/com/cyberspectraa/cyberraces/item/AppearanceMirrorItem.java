package com.cyberspectraa.cyberraces.item;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.OpenAppearanceMirrorPacket;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AppearanceMirrorItem extends Item {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CyberRaces.MOD_ID);
    public static final RegistryObject<Item> MIRROR =
            ITEMS.register("appearance_mirror",
                    () -> new AppearanceMirrorItem(new Item.Properties().stacksTo(1)));

    private AppearanceMirrorItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer server
                && CharacterManager.isCharacterCreated(server)) {
            RaceManager.getBaseRace(server).ifPresent(race ->
                CyberRacesNetwork.sendToPlayer(server,
                    new OpenAppearanceMirrorPacket(race.id(),
                        RaceManager.getEvolution(server).map(e -> e.id()).orElse(""),
                        CharacterManager.getAppearance(server))));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
