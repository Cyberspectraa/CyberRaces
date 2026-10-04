package com.cyberspectraa.cyberraces.mixin;

import com.cyberspectraa.cyberraces.client.ClientCharacterState;
import com.cyberspectraa.cyberraces.client.RaceSkinOverlayManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(
        method = "getSkinTextureLocation",
        at = @At("RETURN"),
        cancellable = true
    )
    private void cyberraces$replaceRaceSkin(
        CallbackInfoReturnable<ResourceLocation> cir
    ) {
        AbstractClientPlayer player =
            (AbstractClientPlayer) (Object) this;

        ResourceLocation original = cir.getReturnValue();
        RaceSkinOverlayManager.rememberOriginal(
            player.getUUID(),
            original
        );

        ClientCharacterState.resolve(player).ifPresent(visual -> {
            ResourceLocation replacement =
                RaceSkinOverlayManager.getReplacement(
                    player,
                    visual.race(),
                    visual.appearance(),
                    original
                );

            cir.setReturnValue(replacement);
        });
    }
}
