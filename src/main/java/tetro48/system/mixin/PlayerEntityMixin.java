package tetro48.system.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private long remainingUnableToConsumeTicks;

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickCustom(CallbackInfo ci) {
        remainingUnableToConsumeTicks--;
    }
    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    private void modifyCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        if (hasStatusEffect(StatusEffects.HUNGER)) {
            if (remainingUnableToConsumeTicks <= 0) {
                remainingUnableToConsumeTicks = 10;
                this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 0.25f, 0.8f + this.random.nextFloat() * 0.7f);
            }
            cir.setReturnValue(false);
        }
        if (!cir.getReturnValue()) {
            if (remainingUnableToConsumeTicks <= 0) {
                remainingUnableToConsumeTicks = 10;
                this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 0.25f, 0.8f + this.random.nextFloat() * 0.7f);
            }
        }
    }
}
