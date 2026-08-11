package tetro48.system.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
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

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	@Unique private float previousExhaustion;
	@Shadow private float exhaustionLevel;

	@Shadow private int foodLevel;

	@Shadow private float saturationLevel;
	@Unique private float previousSaturationLevel;

	@Shadow private int tickTimer;

	@Shadow public abstract int getFoodLevel();

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

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void newUpdate(ServerPlayer serverPlayer, CallbackInfo ci) {
		ServerLevel level = serverPlayer.level();
		Difficulty difficulty = level.getDifficulty();
		if (!isGranular) {
			isGranular = true;
			foodLevel *= 3;
			saturationLevel *= 3;
		}
		maxFoodLevel = Mth.floor(serverPlayer.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		foodLevel = Math.min(foodLevel, maxFoodLevel);
		saturationLevel = Math.min(saturationLevel, maxFoodLevel);
		hungerCostMultiplier = serverPlayer.getAttributeValue(GranularHunger.HUNGER_COST_MULTIPLIER_ATTRIBUTE);
		ServerPlayNetworking.send(serverPlayer, new ExhaustionUpdatePacket(exhaustionLevel - previousExhaustion));
//		switch ()
		burnInBTWStyle();
		if (saturationLevel != previousSaturationLevel) {
			serverPlayer.connection.send(new ClientboundSetHealthPacket(serverPlayer.getHealth(), this.foodLevel, this.saturationLevel));
			previousSaturationLevel = saturationLevel;
		}
		previousExhaustion = exhaustionLevel;
		boolean bl = serverPlayer.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION);
		if (bl && serverPlayer.isHurt() && this.foodLevel > 24) {
			++this.tickTimer;
			if (this.tickTimer >= 400 * healTimeMultiplier[difficulty.getId()]) {
				serverPlayer.heal(1f);
				this.tickTimer = 0;
			}
		}
		else if (this.foodLevel == 0 && this.saturationLevel <= 0) {
			++this.tickTimer;
			if (this.tickTimer >= 80) {
				serverPlayer.hurtServer(level, serverPlayer.damageSources().starve(), 1.0F);
				this.tickTimer = 0;
			}
		}
		else {
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
		float saturationReduction = GranularHunger.getSaturationReduction(foodLevel, maxFoodLevel, nutrition, saturation);
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
