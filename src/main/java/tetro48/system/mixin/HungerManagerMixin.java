package tetro48.system.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tetro48.system.ExhaustionUpdatePacket;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    @Unique private float previousExhaustion;
    @Shadow private float exhaustion;

    @Shadow private int foodLevel;

    @Shadow private float saturationLevel;
    @Unique private float previousSaturationLevel;

    @Shadow private int foodTickTimer;
    @Unique private boolean isGranular;
    @Unique private static final float ONE_AND_ONE_THIRD = 4f/3f;
    @Unique private final float[] healTimeMultiplier = {0.4f, 0.6f, 1f, 1f};

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        saturationLevel = 0f;
    }

    @Inject(method = "isNotFull", at = @At("RETURN"), cancellable = true)
    private void isNotFullUntil60(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.foodLevel < 60);
    }
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void newUpdate(PlayerEntity player, CallbackInfo ci) {
        Difficulty difficulty = player.getWorld().getDifficulty();
        if (!isGranular) {
            isGranular = true;
            foodLevel *= 3;
            saturationLevel *= 3;
        }
        ServerPlayNetworking.send((ServerPlayerEntity) player, new ExhaustionUpdatePacket(exhaustion - previousExhaustion));
        if (Math.ceil(foodLevel/6f) < saturationLevel/6f) {
            float saturationReduce = exhaustion/ONE_AND_ONE_THIRD;
            if (saturationReduce > saturationLevel) {
                exhaustion = (saturationReduce - saturationLevel) * ONE_AND_ONE_THIRD;
                saturationLevel = 0;
            }
            else {
                saturationLevel -= saturationReduce;
                exhaustion = 0;
            }
        }
        if (saturationLevel != previousSaturationLevel) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), this.foodLevel, this.saturationLevel));
            previousSaturationLevel = saturationLevel;
        }
        while (exhaustion > ONE_AND_ONE_THIRD) {
            exhaustion -= ONE_AND_ONE_THIRD;
            this.foodLevel = Math.max(this.foodLevel - 1, 0);
        }
        previousExhaustion = exhaustion;
        boolean bl = player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        if (bl && player.canFoodHeal() && this.foodLevel >= 24) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 400 * healTimeMultiplier[difficulty.getId()]) {
                player.heal(1f);
                this.foodTickTimer = 0;
            }
        }
        else if (this.foodLevel == 0 && this.saturationLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                player.damage(player.getDamageSources().starve(), 1.0F);
                this.foodTickTimer = 0;
            }
        }
        else {
            this.foodTickTimer = 0;
        }
        ci.cancel();
    }
    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I", ordinal = 0))
    private void readCustomNbtAttribute(NbtCompound nbt, CallbackInfo ci) {

        this.isGranular = nbt.getBoolean("is_granular_hunger");
    }
    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putInt(Ljava/lang/String;I)V", ordinal = 0))
    private void writeCustomNbtAttribute(NbtCompound nbt, CallbackInfo ci) {

        nbt.putBoolean("is_granular_hunger", this.isGranular);
    }
    @ModifyConstant(method = "addInternal", constant = @Constant(intValue = 20))
    private int modifyMaxHunger(int constant) {
        return 60;
    }
    @ModifyArg(method = "addInternal", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"))
    private float noWastingSaturation(float value) {
        return 60;
    }
    @Inject(method = "addInternal", at = @At("HEAD"))
    private void modifySaturationGain(int nutrition, float saturation, CallbackInfo ci) {
        if (nutrition <= 0) {
            if (this.foodLevel < 60) saturationLevel -= saturation;
            return;
        }
        int excess = Math.max(this.foodLevel + nutrition - 60, 0);
        float saturationReduction = saturation * (nutrition-excess)/(float)nutrition;
        saturationLevel = Math.max(-saturationReduction, saturationLevel - saturationReduction);
    }
    @ModifyArg(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addInternal(IF)V"), index = 0)
    private int multiplyNutritionResFoodBy3X(int nutrition){
        return nutrition*3;
    }
    @ModifyArg(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addInternal(IF)V"), index = 1)
    private float multiplySaturationResFoodBy3X(float saturation){
        return saturation*3;
    }
}
