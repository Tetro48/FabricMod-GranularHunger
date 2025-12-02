package tetro48.system.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tetro48.system.GranularHunger;
import tetro48.system.HungerManagerHelper;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Unique
	private long remainingUnableToConsumeTicks;

	@Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

	@Shadow protected FoodData foodData;

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickCustom(CallbackInfo ci) {
		remainingUnableToConsumeTicks--;
	}


	@Inject(method = "canEat", at = @At("RETURN"), cancellable = true)
	private void modifyCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
		System.out.println("could eat: " + cir.getReturnValue());
		if (hasEffect(MobEffects.HUNGER)) {
			if (remainingUnableToConsumeTicks <= 0) {
				remainingUnableToConsumeTicks = 10;
				this.playSound(SoundEvents.PLAYER_BURP, 0.25f, 0.8f + this.random.nextFloat() * 0.7f);
			}
			cir.setReturnValue(false);
			return;
		}
		if (!cir.getReturnValue()) {
			if (remainingUnableToConsumeTicks <= 0) {
				remainingUnableToConsumeTicks = 10;
				this.playSound(SoundEvents.PLAYER_BURP, 0.25f, 0.8f + this.random.nextFloat() * 0.7f);
			}
		}
	}
	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void addNewAttribute(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		cir.getReturnValue().add(GranularHunger.HUNGER_COST_MULTIPLIER_ATTRIBUTE);
		cir.getReturnValue().add(GranularHunger.MAX_HUNGER_ATTRIBUTE);
	}
}
