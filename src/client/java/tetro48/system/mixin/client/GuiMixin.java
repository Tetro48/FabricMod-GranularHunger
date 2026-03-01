package tetro48.system.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tetro48.system.GranularHunger;
import tetro48.system.GranularHungerClient;

@Mixin(Gui.class)
public abstract class GuiMixin {

	@Shadow @Final private static Identifier FOOD_EMPTY_SPRITE;

	@Shadow @Final private static Identifier FOOD_FULL_SPRITE;

	@Unique private static final Identifier FOOD_FAT_OUTLINE = Identifier.fromNamespaceAndPath("granular-hunger","hud/food_fat_outline");

	@Shadow private int tickCount;

	@Shadow @Final private static Identifier FOOD_EMPTY_HUNGER_SPRITE;

	@Shadow @Final private static Identifier FOOD_FULL_HUNGER_SPRITE;

	@Unique
	private RandomSource granularHungerRandom = RandomSource.create();

	@Unique
	private long previousTime;

	@Unique private double expDecay(double a, double b, double decay, double dt) {
		return b + (a - b) * Math.exp(-decay * dt);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"), method = "renderPlayerHealth")
	private void modifyRenderFood(Gui instance, GuiGraphics context, Player player, int top, int right) {
		int maxHunger = Mth.floor(player.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		this.granularHungerRandom.setSeed(this.tickCount * 312871L);
		double dt = (Util.getNanos() - previousTime) / 1e9d;
		FoodData hungerManager = player.getFoodData();
		int iFoodLevel = hungerManager.getFoodLevel();
		float fSaturationLevel = hungerManager.getSaturationLevel();
		int iSaturationPips = (int) ((hungerManager.getSaturationLevel() + 0.124F));

		float foodBarShakeTimer = GranularHungerClient.foodBarShakeTimer;
		if (GranularHungerClient.foodBarShakeTimer > 0) {
			GranularHungerClient.foodBarShakeTimer = (float) Math.max(0d, expDecay(GranularHungerClient.foodBarShakeTimer, 0d, 10d, dt));
		}
		for(int j = 0; j < Math.ceilDiv(maxHunger, 6); ++j) {
			int line = j / 10;
			int row = j % 10;
			int lines = Math.ceilDiv(maxHunger, 60);
			int partialHungerPips = Math.min(6, iFoodLevel - j * 6);
			int partialSaturationPips = (int) Math.min(8, ((fSaturationLevel + 0.124f) / 0.75f) - j * 8f);
			int k = top - line * Math.max(11-lines, 4);
			Identifier identifier;
			Identifier identifier1;
			Identifier identifier2;
			if (player.hasEffect(MobEffects.HUNGER)) {
				identifier = FOOD_EMPTY_HUNGER_SPRITE;
				identifier1 = FOOD_FAT_OUTLINE;
				identifier2 = FOOD_FULL_HUNGER_SPRITE;
			} else {
				identifier = FOOD_EMPTY_SPRITE;
				identifier1 = FOOD_FAT_OUTLINE;
				identifier2 = FOOD_FULL_SPRITE;
			}

			if (foodBarShakeTimer > 0.001 || (this.tickCount % (iFoodLevel + 1) == 0 && iFoodLevel < maxHunger / 2)) {
				k += (this.granularHungerRandom.nextInt(3) - 1);
			}

			int l = right - row * 8 - 9;
			if ((j+1) * 6 > maxHunger) {
				int pixelOffset = (maxHunger - (j*6));
				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, 9, 9, 7-pixelOffset, 0, l + (7-pixelOffset), k, pixelOffset+2, 9);
			}
			else {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, l, k, 9, 9);
			}
			if (j * 6 < iSaturationPips) {
				int pixelOffset = Math.max(0, partialSaturationPips);
				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
			if (j * 6 < iFoodLevel) {
				int pixelOffset = Math.max(0, partialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
		}
		renderOverlay(instance, context, player, top, right, maxHunger, foodBarShakeTimer);
		previousTime = Util.getNanos();
	}
	@Unique
	private void renderOverlay(Gui guiInstance, GuiGraphics context, Player player, int top, int right, int maxHunger, float foodBarShakeTimer) {

		ItemStack item = player.getMainHandItem();
		var foodComponent = item.get(DataComponents.FOOD);
		int hungerPips = item.getOrDefault(GranularHunger.HUNGER_PIP_COMPONENT, 0);
		if (foodComponent == null || (foodComponent.nutrition() <= 0 && hungerPips <= 0))
			return;

		int foodLevel = player.getFoodData().getFoodLevel();
		float saturationLevel = player.getFoodData().getSaturationLevel();
		int hungerRestored = foodComponent.nutrition() * 3 + item.getOrDefault(GranularHunger.HUNGER_PIP_COMPONENT, 0);
		float saturationRestored = foodComponent.saturation() * 3;
		if (player.hasEffect(MobEffects.HUNGER)) {
			return;
		}
		this.granularHungerRandom.setSeed(this.tickCount * 312871L);
		Identifier identifier;
		Identifier identifier1;
		Identifier identifier2;
		if (player.hasEffect(MobEffects.HUNGER)) {
			identifier = FOOD_EMPTY_HUNGER_SPRITE;
			identifier1 = FOOD_FAT_OUTLINE;
			identifier2 = FOOD_FULL_HUNGER_SPRITE;
		} else {
			identifier = FOOD_EMPTY_SPRITE;
			identifier1 = FOOD_FAT_OUTLINE;
			identifier2 = FOOD_FULL_SPRITE;
		}
		int alphaColor = 0x00_FFFFFF;

		alphaColor |= (int)Math.abs(Math.sin(Util.getNanos() / 4e8d) * 192) << 24;

		int modifiedFood = Math.clamp(foodLevel + hungerRestored, 0, maxHunger);
		float modifiedSaturation = Math.clamp(saturationLevel + saturationRestored - GranularHunger.getSaturationReduction(foodLevel, maxHunger, hungerRestored, saturationRestored), 0, maxHunger);

		int startFoodBars = (int) Math.min(Math.max(0, foodLevel / 6), Math.max(0, saturationLevel / 6f));
		int endFoodBars = (int) Math.min(Math.ceilDiv(maxHunger, 6), Math.max(Math.ceil(modifiedFood / 6.0F), Math.ceil(modifiedSaturation / 6.0F)));

		int iconSize = 9;

		granularHungerRandom.consumeCount(startFoodBars);
//		context.drawCenteredString(guiInstance.getFont(), String.format("debug: sat.:%f, sat. red:%f, sat.res:%f", saturationLevel, GranularHunger.getSaturationReduction(foodLevel, maxHunger, hungerRestored, saturationRestored), saturationRestored), top, right, 0xffffffff);
		for (int i = startFoodBars; i < endFoodBars; ++i)
		{
			int line = i / 10;
			int row = i % 10;
			int lines = Math.ceilDiv(maxHunger, 60);
			int partialHungerPips = Math.min(6, modifiedFood - i * 6);
			int partialSaturationPips = (int) Math.min(8, ((modifiedSaturation + 0.124f) / 0.75f) - i * 8f);
			int k = top - line * Math.max(11-lines, 4);

			if (foodBarShakeTimer > 0.001 || (this.tickCount % (foodLevel + 1) == 0 && foodLevel < maxHunger / 2)) {
				k += (this.granularHungerRandom.nextInt(3) - 1);
			}

			int l = right - row * 8 - 9;
//			if ((i+1) * 6 > maxHunger) {
//				int pixelOffset = (maxHunger - (i*6));
//				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, 9, 9, 7-pixelOffset, 0, l + (7-pixelOffset), k, pixelOffset+2, 9, alphaColor);
//			}
//			else {
//				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, l, k, 9, 9, alphaColor);
//			}
			if (i * 6 < modifiedSaturation) {
				int pixelOffset = Math.max(0, partialSaturationPips);
				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9, alphaColor);
			}
			if (i * 6 < modifiedFood) {
				int pixelOffset = Math.max(0, partialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9, alphaColor);
			}
		}
	}
}
