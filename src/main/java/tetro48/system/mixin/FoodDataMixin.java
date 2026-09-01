package tetro48.system.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tetro48.system.ExhaustionUpdatePacket;
import tetro48.system.GranularHunger;
import tetro48.system.HungerBehaviorUpdatePacket;
import tetro48.system.HungerSystemBehaviorMode;
import tetro48.system.configs.GranularHungerConfigManager;
import tetro48.system.configs.HungerSystemConfig;

import java.util.function.Supplier;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	@Unique private float previousExhaustion;
	@Shadow private float exhaustionLevel;

	@Shadow private int foodLevel;

	@Shadow private float saturationLevel;
	@Unique private float previousSaturationLevel;

	@Shadow private int tickTimer;

	@Shadow public abstract int getFoodLevel();

	@Shadow
	public abstract void addExhaustion(float amount);

	@Unique private boolean isGranular;
	@Unique private static final float ONE_AND_ONE_THIRD = 4f/3f;
	@Unique private int maxFoodLevel = 60;
	@Unique private double hungerCostMultiplier = 1d;

	@Unique private HungerSystemBehaviorMode previousBehaviorMode = null;

	@Unique
	private static final Supplier<HungerSystemConfig> CONFIG = () -> GranularHungerConfigManager.get().hungerSystem;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (CONFIG.get().behaviorMode == HungerSystemBehaviorMode.BTW_MODE) {
			saturationLevel = 0f;
		}
	}

	@Inject(method = "needsFood", at = @At("RETURN"), cancellable = true)
	private void isNotFullUntil60(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.foodLevel < maxFoodLevel);
	}
	@Inject(method = "hasEnoughFood", at = @At("HEAD"), cancellable = true)
	private void adaptHasEnoughFood(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.getFoodLevel() > 18F);
	}

	@Unique
	private boolean burnInBTWStyle() {
		boolean	doesFatBurn = Math.ceil(foodLevel/6f) < saturationLevel/6f;
		if (doesFatBurn) {
			float saturationReduce = 1 / ONE_AND_ONE_THIRD;
			if (saturationReduce > saturationLevel) {
				exhaustionLevel = (saturationReduce - saturationLevel) * ONE_AND_ONE_THIRD;
				saturationLevel = 0;
			}
			else {
				saturationLevel -= saturationReduce;
				exhaustionLevel -= 1;
			}
		}
		else {
			exhaustionLevel -= ONE_AND_ONE_THIRD;
			this.foodLevel = Math.max(this.foodLevel - 1, 0);
		}
		return doesFatBurn;
	}

	@Unique
	private boolean burnInVanillaStyle() {
		if (saturationLevel > 0) {
			float saturationReduce = 1 / ONE_AND_ONE_THIRD;
			if (saturationReduce > saturationLevel) {
				exhaustionLevel = (saturationReduce - saturationLevel) * ONE_AND_ONE_THIRD;
				saturationLevel = 0;
			}
			else {
				saturationLevel -= saturationReduce;
				exhaustionLevel -= 1;
			}
		}
		else {
			exhaustionLevel -= ONE_AND_ONE_THIRD;
			this.foodLevel = Math.max(this.foodLevel - 1, 0);
		}
		return exhaustionLevel >= ONE_AND_ONE_THIRD;
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void newUpdate(ServerPlayer player, CallbackInfo ci) {
		HungerSystemBehaviorMode behaviorMode = CONFIG.get().behaviorMode;
		ServerLevel level = player.level();
		Difficulty difficulty = level.getDifficulty();
		if (!isGranular) {
			isGranular = true;
			foodLevel *= 3;
			saturationLevel *= 3;
		}
		maxFoodLevel = Mth.floor(player.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		foodLevel = Math.min(foodLevel, maxFoodLevel);
		saturationLevel = Math.min(saturationLevel, maxFoodLevel);
		hungerCostMultiplier = player.getAttributeValue(GranularHunger.HUNGER_COST_MULTIPLIER_ATTRIBUTE);
		if (previousBehaviorMode != behaviorMode) {
			previousBehaviorMode = behaviorMode;
			ServerPlayNetworking.send(player, new HungerBehaviorUpdatePacket(behaviorMode.name()));
		}
		if (exhaustionLevel != previousExhaustion) {
			ServerPlayNetworking.send(player, new ExhaustionUpdatePacket(exhaustionLevel - previousExhaustion));
		}
		while (exhaustionLevel > ONE_AND_ONE_THIRD && switch (behaviorMode) {
			case BTW_MODE -> burnInBTWStyle();
			case VANILLA_MODE -> burnInVanillaStyle();
		});
		if (saturationLevel != previousSaturationLevel) {
			player.connection.send(new ClientboundSetHealthPacket(player.getHealth(), this.foodLevel, this.saturationLevel));
			previousSaturationLevel = saturationLevel;
		}
		previousExhaustion = exhaustionLevel;
		boolean naturalRegen = player.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION);
		if (behaviorMode == HungerSystemBehaviorMode.BTW_MODE) {
			if (naturalRegen && player.isHurt() && this.foodLevel > 24) {
				++this.tickTimer;
				if (this.tickTimer >= GranularHunger.getTicksUntilHeal(level)) {
					player.heal(1f);
					this.tickTimer = 0;
				}
			}
		} else if (behaviorMode == HungerSystemBehaviorMode.VANILLA_MODE) {
			if (naturalRegen && this.saturationLevel > 0.0F && player.isHurt() && this.foodLevel >= maxFoodLevel) {
				++this.tickTimer;
				if (this.tickTimer >= 10) {
					float saturationSpent = Math.min(this.saturationLevel, 6.0F);
					player.heal(saturationSpent / 6.0F);
					this.addExhaustion(saturationSpent);
					this.tickTimer = 0;
				}
			} else if (naturalRegen && this.foodLevel >= maxFoodLevel * 0.9d && player.isHurt()) {
				++this.tickTimer;
				if (this.tickTimer >= 80) {
					player.heal(1.0F);
					this.addExhaustion(6.0F);
					this.tickTimer = 0;
				}
			}
		}
		if (this.foodLevel == 0 && this.saturationLevel <= 0) {
			++this.tickTimer;
			if (this.tickTimer >= 80) {
				player.hurtServer(level, player.damageSources().starve(), 1.0F);
				this.tickTimer = 0;
			}
		}
		else if (!naturalRegen || !player.isHurt()) {
			this.tickTimer = 0;
		}
		ci.cancel();
	}
	@Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ValueInput;getIntOr(Ljava/lang/String;I)I", ordinal = 0))
	private void readCustomNbtAttribute(ValueInput valueInput, CallbackInfo ci) {

		this.isGranular = valueInput.getBooleanOr("is_granular_hunger", false);
	}
	@Inject(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ValueOutput;putInt(Ljava/lang/String;I)V", ordinal = 0))
	private void writeCustomNbtAttribute(ValueOutput valueOutput, CallbackInfo ci) {

		valueOutput.putBoolean("is_granular_hunger", this.isGranular);
	}
	@ModifyConstant(method = "add", constant = @Constant(intValue = 20))
	private int modifyMaxHunger(int constant) {
		return maxFoodLevel;
	}
	@ModifyArg(method = "add", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	private float noWastingSaturation(float value) {
		if (CONFIG.get().behaviorMode == HungerSystemBehaviorMode.VANILLA_MODE) {
			return value;
		}
		return maxFoodLevel;
	}
	@Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
	private void modifyExhaustionGain(float exhaustion, CallbackInfo ci) {
		this.exhaustionLevel += (float) (exhaustion * hungerCostMultiplier);
		ci.cancel();
	}
	@Inject(method = "add", at = @At("HEAD"))
	private void modifySaturationGain(int food, float saturation, CallbackInfo ci) {
		if (CONFIG.get().behaviorMode != HungerSystemBehaviorMode.BTW_MODE) return;
		if (food <= 0) {
			if (this.foodLevel < maxFoodLevel) saturationLevel -= saturation;
			return;
		}
		float saturationReduction = GranularHunger.getSaturationReduction(foodLevel, maxFoodLevel, food, saturation);
		saturationLevel = Math.max(-saturationReduction, saturationLevel - saturationReduction);
	}
	@ModifyArg(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;add(IF)V"), index = 0)
	private int multiplyNutritionResFoodBy3X(int nutrition){
		return nutrition*3;
	}
	@ModifyArg(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;add(IF)V"), index = 1)
	private float multiplySaturationResFoodBy3X(float saturation){
		return saturation*3;
	}
}
