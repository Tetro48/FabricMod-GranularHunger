package tetro48.system.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tetro48.system.GranularHunger;
import tetro48.system.HungerManagerHelper;

@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin {
	//this order of arguments matter, cuz, how tf are you gon- also, world arg isn't used, but it's mandatory.
	@Redirect(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
	private void onEatFood(FoodData hungerManager, FoodProperties foodComponent, Level world, LivingEntity livingEntity, ItemStack stack) {
		int hungerPips = stack.getOrDefault(GranularHunger.HUNGER_PIP_COMPONENT, 0);
		HungerManagerHelper.eatCombined(hungerManager, foodComponent, hungerPips);
	}
}
