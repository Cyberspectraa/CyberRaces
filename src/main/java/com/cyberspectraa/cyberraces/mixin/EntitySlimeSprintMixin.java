package com.cyberspectraa.cyberraces.mixin;

import com.cyberspectraa.cyberraces.client.ClientCharacterState;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the usual running block-dust particle with vanilla slime
 * droplets for both player and tracked Wild-NPC Slimes. Client-only.
 */
@Mixin(Entity.class)
public abstract class EntitySlimeSprintMixin {
    @Inject(method = "spawnSprintParticle", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void cyberraces$slimeSprint(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!entity.level().isClientSide
                || ClientCharacterState.resolve(entity.getUUID())
                        .map(visual -> visual.race() == Race.SLIME && !visual.undead())
                        .orElse(false) == false) return;
        ci.cancel();
        if (entity.tickCount % 2 != 0) return;
        entity.level().addParticle(ParticleTypes.ITEM_SLIME,
                entity.getX() + (entity.level().random.nextDouble() - 0.5D) * entity.getBbWidth(),
                entity.getY() + 0.06D,
                entity.getZ() + (entity.level().random.nextDouble() - 0.5D) * entity.getBbWidth(),
                (entity.level().random.nextDouble() - 0.5D) * 0.04D, 0.015D,
                (entity.level().random.nextDouble() - 0.5D) * 0.04D);
    }
}
