package tetro48.system.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tetro48.system.ExhaustionUpdatePacket;
import tetro48.system.GranularHunger;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	@Unique private float previousExhaustion;
	@Shadow private float exhaustionLevel;

	@Shadow private int foodLevel;

	@Shadow private float saturationLevel;
	@Unique private float previousSaturationLevel;

	@Shadow private int tickTimer;
	@Unique private boolean isGranular;
	@Unique private static final float ONE_AND_ONE_THIRD = 4f/3f;
	@Unique private final float[] healTimeMultiplier = {0.4f, 0.6f, 1f, 1f};
	@Unique private int maxFoodLevel = 60;
	@Unique private double hungerCostMultiplier = 1d;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		saturationLevel = 0f;
	}

	@Inject(method = "needsFood", at = @At("RETURN"), cancellable = true)
	private void isNotFullUntil60(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.foodLevel < maxFoodLevel);
	}
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void newUpdate(Player player, CallbackInfo ci) {
		Difficulty difficulty = player.level().getDifficulty();
		if (!isGranular) {
			isGranular = true;
			foodLevel *= 3;
			saturationLevel *= 3;
		}
		maxFoodLevel = Mth.floor(player.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		foodLevel = Math.min(foodLevel, maxFoodLevel);
		saturationLevel = Math.min(saturationLevel, maxFoodLevel);
		hungerCostMultiplier = player.getAttributeValue(GranularHunger.HUNGER_COST_MULTIPLIER_ATTRIBUTE);
		ServerPlayNetworking.send((ServerPlayer) player, new ExhaustionUpdatePacket(exhaustionLevel - previousExhaustion));
		if (Math.ceil(foodLevel/6f) < saturationLevel/6f) {
			float saturationReduce = exhaustionLevel /ONE_AND_ONE_THIRD;
			if (saturationReduce > saturationLevel) {
				exhaustionLevel = (saturationReduce - saturationLevel) * ONE_AND_ONE_THIRD;
				saturationLevel = 0;
			}
			else {
				saturationLevel -= saturationReduce;
				exhaustionLevel = 0;
			}
		}
		if (saturationLevel != previousSaturationLevel) {
			((ServerPlayer) player).connection.send(new ClientboundSetHealthPacket(player.getHealth(), this.foodLevel, this.saturationLevel));
			previousSaturationLevel = saturationLevel;
		}
		while (exhaustionLevel > ONE_AND_ONE_THIRD) {
			exhaustionLevel -= ONE_AND_ONE_THIRD;
			this.foodLevel = Math.max(this.foodLevel - 1, 0);
		}
		previousExhaustion = exhaustionLevel;
		boolean bl = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
		if (bl && player.isHurt() && this.foodLevel > 24) {
			++this.tickTimer;
			if (this.tickTimer >= 400 * healTimeMultiplier[difficulty.getId()]) {
				player.heal(1f);
				this.tickTimer = 0;
			}
		}
		else if (this.foodLevel == 0 && this.saturationLevel <= 0) {
			++this.tickTimer;
			if (this.tickTimer >= 80) {
				player.hurt(player.damageSources().starve(), 1.0F);
				this.tickTimer = 0;
			}
		}
		else {
			this.tickTimer = 0;
		}
		ci.cancel();
	}
	@Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getInt(Ljava/lang/String;)I", ordinal = 0))
	private void readCustomNbtAttribute(CompoundTag nbt, CallbackInfo ci) {

		this.isGranular = nbt.getBoolean("is_granular_hunger");
	}
	@Inject(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V", ordinal = 0))
	private void writeCustomNbtAttribute(CompoundTag nbt, CallbackInfo ci) {

		nbt.putBoolean("is_granular_hunger", this.isGranular);
	}
	@ModifyConstant(method = "add", constant = @Constant(intValue = 20))
	private int modifyMaxHunger(int constant) {
		return maxFoodLevel;
	}
	@ModifyArg(method = "add", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	private float noWastingSaturation(float value) {
		return maxFoodLevel;
	}
	@Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
	private void modifyExhaustionGain(float exhaustion, CallbackInfo ci) {
		this.exhaustionLevel += (float) (exhaustion * hungerCostMultiplier);
		ci.cancel();
	}
	@Inject(method = "add", at = @At("HEAD"))
	private void modifySaturationGain(int nutrition, float saturation, CallbackInfo ci) {
		if (nutrition <= 0) {
			if (this.foodLevel < maxFoodLevel) saturationLevel -= saturation;
			return;
		}
		int excess = Math.max(this.foodLevel + nutrition - maxFoodLevel, 0);
		float saturationReduction = saturation * (nutrition-excess)/(float)nutrition;
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
