package tetro48.system.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tetro48.system.GranularHunger;
import tetro48.system.GranularHungerClient;

@Mixin(Gui.class)
public abstract class GuiMixin {

	@Shadow @Final private static ResourceLocation FOOD_EMPTY_SPRITE;

	@Shadow @Final private static ResourceLocation FOOD_FULL_SPRITE;

	@Unique private static final ResourceLocation FOOD_FAT_OUTLINE = ResourceLocation.fromNamespaceAndPath("granular-hunger","hud/food_fat_outline");

	@Shadow private int tickCount;

	@Shadow @Final private static ResourceLocation FOOD_EMPTY_HUNGER_SPRITE;

	@Shadow @Final private static ResourceLocation FOOD_FULL_HUNGER_SPRITE;

	@Shadow
	@Nullable
	protected abstract Player getCameraPlayer();

	@Unique
	private RandomSource granularHungerRandom = RandomSource.create();

	@Unique
	private long previousTime;

	@Unique private double expDecay(double a, double b, double decay, double dt) {
		return b + (a - b) * Math.exp(-decay * dt);
	}

	@ModifyArg(method = "renderPlayerHealth", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
	private int offsetAccordingly(int constant) {
		Player playerEntity = this.getCameraPlayer();
		int maxHunger = 60;
		if (playerEntity != null) {
			maxHunger = Mth.floor(this.getCameraPlayer().getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		}
		int lines = Math.ceilDiv(maxHunger, 60) - 1;
		int offset = Math.max(10-lines, 4);
		return constant - (lines * offset);
	}

	@Inject(at = @At("HEAD"), method = "renderFood", cancellable = true)
	private void modifyRenderFood(GuiGraphics guiGraphics, Player player, int top, int right, CallbackInfo ci) {
		int maxHunger = Mth.floor(player.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		this.granularHungerRandom.setSeed(this.tickCount * 312871L);
		guiGraphics.setColor(1f, 1f, 1f, 1f);
		double dt = (Util.getNanos() - previousTime) / 1e9d;
		FoodData hungerManager = player.getFoodData();
		int iFoodLevel = hungerManager.getFoodLevel();
		float fSaturationLevel = hungerManager.getSaturationLevel();
		int iSaturationPips = (int) ((hungerManager.getSaturationLevel() + 0.124F));

		double foodBarShakeTimer = Math.max(GranularHungerClient.foodBarShakeTimer, GranularHungerClient.forcedShakeTime);
		if (GranularHungerClient.foodBarShakeTimer > 0) {
			GranularHungerClient.foodBarShakeTimer = Math.max(0d, expDecay(GranularHungerClient.foodBarShakeTimer, 0d, 10d, dt));
		}
		GranularHungerClient.forcedShakeTime -= dt;
		RenderSystem.enableBlend();
		for(int j = 0; j < Math.ceilDiv(maxHunger, 6); ++j) {
			int line = j / 10;
			int row = j % 10;
			int lines = Math.ceilDiv(maxHunger, 60);
			int partialHungerPips = Math.min(6, iFoodLevel - j * 6);
			int partialSaturationPips = (int) Math.min(8, ((fSaturationLevel + 0.124f) / 0.75f) - j * 8f);
			int k = top - line * Math.max(11-lines, 4);
			ResourceLocation identifier;
			ResourceLocation identifier1;
			ResourceLocation identifier2;
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
				guiGraphics.blitSprite(identifier, 9, 9, 7-pixelOffset, 0, l + (7-pixelOffset), k, pixelOffset+2, 9);
			}
			else {
				guiGraphics.blitSprite(identifier, l, k, 9, 9);
			}
			if (j * 6 < iSaturationPips) {
				int pixelOffset = Math.max(0, partialSaturationPips);
				guiGraphics.blitSprite(identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
			if (j * 6 < iFoodLevel) {
				int pixelOffset = Math.max(0, partialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				guiGraphics.blitSprite(identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
		}
		RenderSystem.disableBlend();
		renderOverlay(guiGraphics, player, top, right, maxHunger, foodBarShakeTimer);
		previousTime = Util.getNanos();
		ci.cancel();
	}
	@Unique
	private void renderOverlay(GuiGraphics context, Player player, int top, int right, int maxHunger, double foodBarShakeTimer) {

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
		ResourceLocation identifier;
		ResourceLocation identifier1;
		ResourceLocation identifier2;
		if (player.hasEffect(MobEffects.HUNGER)) {
			identifier = FOOD_EMPTY_HUNGER_SPRITE;
			identifier1 = FOOD_FAT_OUTLINE;
			identifier2 = FOOD_FULL_HUNGER_SPRITE;
		} else {
			identifier = FOOD_EMPTY_SPRITE;
			identifier1 = FOOD_FAT_OUTLINE;
			identifier2 = FOOD_FULL_SPRITE;
		}

		int modifiedFood = Math.clamp(foodLevel + hungerRestored, 0, maxHunger);
		float modifiedSaturation = Math.clamp(saturationLevel + saturationRestored - GranularHunger.getSaturationReduction(foodLevel, maxHunger, hungerRestored, saturationRestored), 0, maxHunger);

		int startFoodBars = (int) Math.min(Math.max(0, foodLevel / 6), Math.max(0, saturationLevel / 6f));
		int endFoodBars = (int) Math.min(Math.ceilDiv(maxHunger, 6), Math.max(Math.ceil(modifiedFood / 6.0F), Math.ceil(modifiedSaturation / 6.0F)));

		int iconSize = 9;

		RenderSystem.enableBlend();
		granularHungerRandom.consumeCount(startFoodBars);
		context.setColor(1f, 1f, 1f, (float) Math.abs(Math.sin(Util.getNanos() / 4e8d) * 0.75f));
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
			if (i * 6 < modifiedSaturation) {
				int pixelOffset = Math.max(0, partialSaturationPips);
				context.blitSprite(identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
			if (i * 6 < modifiedFood) {
				int pixelOffset = Math.max(0, partialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				context.blitSprite(identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
		}
		context.setColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
	}
}
