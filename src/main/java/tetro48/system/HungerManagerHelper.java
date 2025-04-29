package tetro48.system;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.HungerManager;

public class HungerManagerHelper {
	public static void eatCombined(HungerManager hungerManager, FoodComponent foodComponent, int hungerPips) {
		hungerManager.addInternal(foodComponent.nutrition() * 3 + hungerPips, foodComponent.saturation() * 3);
	}
}
