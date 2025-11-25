package tetro48.system;

import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;

public class HungerManagerHelper {
	public static void eatCombined(FoodData hungerManager, FoodProperties foodComponent, int hungerPips) {
		hungerManager.add(foodComponent.nutrition() * 3 + hungerPips, foodComponent.saturation() * 3);
	}
}
