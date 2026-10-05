package com.cyberspectraa.cyberraces.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {
    @ModifyVariable(
        method = "causeFoodExhaustion",
        at = @At("HEAD"),
        argsOnly = true
    )
    private float cyberraces$dogfolkSprintEndurance(
        float exhaustion
    ) {
        Player player = (Player) (Object) this;

        if (player.level().isClientSide
            || !player.isSprinting()) {
            return exhaustion;
        }

        CompoundTag root =
            player.getPersistentData().getCompound("CyberRaces");

        if ("dogfolk".equals(root.getString("Race"))) {
            return exhaustion * 0.65F;
        }

        return exhaustion;
    }
}
