package tetro48.system.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tetro48.system.GranularHunger;
import tetro48.system.HungerSystemBehaviorMode;
import tetro48.system.client.GranularHungerClient;
import tetro48.system.client.config.GranularHungerClientConfigManager;
import tetro48.system.configs.HungerDisplayConfig;

import java.util.function.Supplier;

@Mixin(Hud.class)
public abstract class HudMixin {

	@Shadow @Final private static Identifier FOOD_EMPTY_SPRITE;

	@Shadow @Final private static Identifier FOOD_FULL_SPRITE;

	@Unique private static final Identifier FOOD_FAT_OUTLINE = Identifier.fromNamespaceAndPath("granular-hunger","hud/food_fat_outline");

	@Shadow private int tickCount;

	@Shadow @Final private static Identifier FOOD_EMPTY_HUNGER_SPRITE;

	@Shadow @Final private static Identifier FOOD_FULL_HUNGER_SPRITE;

	@Shadow
	@Nullable
	protected abstract Player getCameraPlayer();

	@Shadow
	@Nullable
	protected abstract LivingEntity getPlayerVehicleWithHealth();

	@Shadow
	protected abstract int getVehicleMaxHearts(@Nullable LivingEntity vehicle);

	@Unique
	private RandomSource granularHungerRandom = RandomSource.create();

	@Unique
	private static final Supplier<HungerDisplayConfig> CONFIG = () -> GranularHungerClientConfigManager.get().hungerDisplay;

	@Unique
	private long previousTime;

	@Unique private double expDecay(double a, double b, double decay, double dt) {
		return b + (a - b) * Math.exp(-decay * dt);
	}

	@ModifyArg(method = "extractPlayerHealth", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"))
	private int offsetAccordingly(int constant) {
		LivingEntity vehicleWithHearts = this.getPlayerVehicleWithHealth();
		int vehicleHearts = this.getVehicleMaxHearts(vehicleWithHearts);
		if (vehicleHearts != 0) {
			return constant;
		}
		Player playerEntity = this.getCameraPlayer();
		int maxHunger = 60;
		if (playerEntity != null) {
			maxHunger = Mth.floor(this.getCameraPlayer().getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		}
		int lines = Math.ceilDiv(maxHunger, 60) - 1;
		int offset = Math.max(10-lines, 4);
		return constant - (lines * offset);
	}

	@Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
	private void modifyRenderFood(final GuiGraphicsExtractor context, final Player player, final int top, final int right, CallbackInfo ci) {
		int maxHunger = Mth.floor(player.getAttributeValue(GranularHunger.MAX_HUNGER_ATTRIBUTE));
		if (granularHungerRandom == null) {
			granularHungerRandom = RandomSource.create(); // just in case
		}
		this.granularHungerRandom.setSeed(this.tickCount * 312871L);
		double dt = (Util.getNanos() - previousTime) / 1e9d;
		FoodData hungerManager = player.getFoodData();
		int iFoodLevel = hungerManager.getFoodLevel();
		float fSaturationLevel = hungerManager.getSaturationLevel();
		int iSaturationPips = (int) ((hungerManager.getSaturationLevel() + 0.124F));

		ItemStack item = player.getMainHandItem();
		var foodComponent = item.get(DataComponents.FOOD);
		int hungerPips = item.getOrDefault(GranularHunger.HUNGER_PIP_COMPONENT, 0);

		int hungerRestored = 0;
		float saturationRestored = 0;

		// Display Overlay setting now deals with this, and it's an elegant solution, considering the overlay is now weaved in.
		if (CONFIG.get().displayOverlay && foodComponent != null && !player.hasEffect(MobEffects.HUNGER) && (iFoodLevel != maxHunger || foodComponent.canAlwaysEat())) {
			hungerRestored = foodComponent.nutrition() * 3 + hungerPips;
			saturationRestored = foodComponent.saturation() * 3;
		}

		int fatColor = CONFIG.get().fatColor.getRGB();
		int alphaColor = 0x00_FFFFFF;
		int fatColorWithAlpha = fatColor & 0x00_FFFFFF;

		alphaColor |= (int)Math.abs(Math.sin(Util.getNanos() / 4e8d) * 192) << 24;
		fatColorWithAlpha |= (int)Math.abs(Math.sin(Util.getNanos() / 4e8d) * 192) << 24;

		double foodBarShakeTimer = Math.max(GranularHungerClient.foodBarShakeTimer, GranularHungerClient.forcedShakeTime);
		if (GranularHungerClient.foodBarShakeTimer > 0) {
			GranularHungerClient.foodBarShakeTimer = Math.max(0d, expDecay(GranularHungerClient.foodBarShakeTimer, 0d, 10d, dt));
		}
		if (!CONFIG.get().hungerShakeOnExhaustion) {
			foodBarShakeTimer = 0;
		}
		GranularHungerClient.forcedShakeTime -= dt;
		int overlayFood = Math.clamp(iFoodLevel + hungerRestored, 0, maxHunger);
		float overlaySaturation = Math.clamp(fSaturationLevel + saturationRestored - GranularHungerClient.getSaturationReduction(iFoodLevel, maxHunger, hungerRestored, saturationRestored), 0, maxHunger);

		for(int j = 0; j < Math.ceilDiv(maxHunger, 6); ++j) {
			int line = j / 10;
			int row = j % 10;
			int lines = Math.ceilDiv(maxHunger, 60);
			int partialHungerPips = Math.min(6, iFoodLevel - j * 6);
			int partialSaturationPips = (int) Math.min(8, ((fSaturationLevel + 0.124f) / 0.75f) - j * 8f);
			int overlayPartialHungerPips = Math.min(6, overlayFood - j * 6);
			int overlayPartialSaturationPips = (int) Math.min(8, ((overlaySaturation + 0.124f) / 0.75f) - j * 8f);
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
				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9, fatColor);
			}
			if (j * 6 < overlaySaturation && overlayPartialSaturationPips > partialSaturationPips) {
				int pixelOffset = Math.max(0, overlayPartialSaturationPips);
				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9, fatColorWithAlpha);
			}
			if (j * 6 < iFoodLevel) {
				int pixelOffset = Math.max(0, partialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
			}
			if (j * 6 < overlayFood && overlayPartialHungerPips > partialHungerPips) {
				int pixelOffset = Math.max(0, overlayPartialHungerPips) + 1;
				if (pixelOffset == 1) pixelOffset = 2;

				context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9, alphaColor);
			}
		}
		previousTime = Util.getNanos();
		ci.cancel();
	}
}
