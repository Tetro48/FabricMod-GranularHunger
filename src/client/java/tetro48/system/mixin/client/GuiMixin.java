package tetro48.system.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
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

	@Shadow @Final private static ResourceLocation FOOD_EMPTY_SPRITE;

	@Shadow @Final private static ResourceLocation FOOD_FULL_SPRITE;

	@Unique private static final ResourceLocation FOOD_FAT_OUTLINE = ResourceLocation.fromNamespaceAndPath("granular-hunger","hud/food_fat_outline");

	@Final
	@Shadow
	private RandomSource random;

	@Shadow private int tickCount;

	@Shadow @Final private static ResourceLocation FOOD_EMPTY_HUNGER_SPRITE;

	@Shadow @Final private static ResourceLocation FOOD_FULL_HUNGER_SPRITE;


	@Unique
	private long previousTime;

	@Unique private double expDecay(double a, double b, double decay, double dt) {
		return b + (a - b) * Math.exp(-decay * dt);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"), method = "renderPlayerHealth")
	private void modifyRenderFood(Gui instance, GuiGraphics context, Player player, int top, int right) {
		int maxHunger = Mth.floor(player.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		double dt = (Util.getNanos() - previousTime) / 1e9d;
		FoodData hungerManager = player.getFoodData();
		int iFoodLevel = hungerManager.getFoodLevel();
		float fSaturationLevel = hungerManager.getSaturationLevel();
		int iSaturationPips = (int) ((hungerManager.getSaturationLevel() + 0.124F));

		float foodBarShakeTimer = GranularHungerClient.foodBarShakeTimer;
		if (GranularHungerClient.foodBarShakeTimer > 0) {
			GranularHungerClient.foodBarShakeTimer = (float) Math.max(0d, expDecay(GranularHungerClient.foodBarShakeTimer, 0d, 10d, dt));
		}
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
				k += (this.random.nextInt(3) - 1);
			}

			int l = right - row * 8 - 9;
			if ((j+1) * 6 > maxHunger) {
				int pixelOffset = (maxHunger - (j*6));
				context.blitSprite(identifier, 9, 9, 7-pixelOffset, 0, l + (7-pixelOffset), k, pixelOffset+2, 9);
			}
			else {
				context.blitSprite(identifier, l, k, 9, 9);
			}
			if (j * 6 < iSaturationPips) {
				int pixelOffset = Math.max(0, partialSaturationPips);
				context.blitSprite(identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
			if (j * 6 < iFoodLevel) {
				int pixelOffset = Math.max(0, partialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				context.blitSprite(identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
		}
		RenderSystem.disableBlend();
		previousTime = Util.getNanos();
	}

}
