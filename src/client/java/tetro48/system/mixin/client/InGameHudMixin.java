package tetro48.system.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tetro48.system.GranularHungerClient;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Final
    @Shadow private static Identifier FOOD_EMPTY_TEXTURE;
    @Final
    @Shadow private static Identifier FOOD_FULL_TEXTURE;

    @Unique private static final Identifier FOOD_FAT_OUTLINE = Identifier.of("granular-hunger","hud/food_fat_outline");

    @Final
    @Shadow
    private Random random;

    @Shadow private int ticks;

    @Shadow @Final private static Identifier FOOD_EMPTY_HUNGER_TEXTURE;

    @Shadow @Final private static Identifier FOOD_FULL_HUNGER_TEXTURE;

    @Shadow @Final private MinecraftClient client;

    @Unique
    private long previousTime;

    @Unique private double expDecay(double a, double b, double decay, double dt) {
        return b + (a - b) * Math.exp(-decay * dt);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderFood(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;II)V"), method = "renderStatusBars")
    private void modifyRenderFood(InGameHud instance, DrawContext context, PlayerEntity player, int top, int right) {
        double dt = (Util.getMeasuringTimeNano() - previousTime) / 1e9d;
        HungerManager hungerManager = player.getHungerManager();
        int iFoodLevel = hungerManager.getFoodLevel();
        float fSaturationLevel = hungerManager.getSaturationLevel();
        int iSaturationPips = (int) ((hungerManager.getSaturationLevel() + 0.124F));

        int iFullHungerPips = iFoodLevel / 2;

        float foodBarShakeTimer = GranularHungerClient.foodBarShakeTimer;
        if (GranularHungerClient.foodBarShakeTimer > 0) {
            GranularHungerClient.foodBarShakeTimer = (float) Math.max(0d, expDecay(GranularHungerClient.foodBarShakeTimer, 0d, 10d, dt));
        }
        RenderSystem.enableBlend();
        for(int j = 0; j < 10; ++j) {
            int partialHungerPips = Math.min(6, iFoodLevel - j * 6);
            int partialSaturationPips = (int) Math.min(8, (fSaturationLevel / 0.75f) - j * 8f);
            int k = top;
            Identifier identifier;
            Identifier identifier1;
            Identifier identifier2;
            if (player.hasStatusEffect(StatusEffects.HUNGER)) {
                identifier = FOOD_EMPTY_HUNGER_TEXTURE;
                identifier1 = FOOD_FAT_OUTLINE;
                identifier2 = FOOD_FULL_HUNGER_TEXTURE;
//                foodBarShakeTimer = 1f/3f;
            } else {
                identifier = FOOD_EMPTY_TEXTURE;
                identifier1 = FOOD_FAT_OUTLINE;
                identifier2 = FOOD_FULL_TEXTURE;
            }

            if (foodBarShakeTimer > 0.001 || (this.ticks % (iFoodLevel + 1) == 0)) {
                k = top + (this.random.nextInt(3) - 1);
            }

            int l = right - j * 8 - 9;
            context.drawGuiTexture(identifier, l, k, 9, 9);
            if (j * 6 < fSaturationLevel) {
                int pixelOffset = Math.max(0, partialSaturationPips) + 1;
//                if (pixelOffset == 1) pixelOffset = 2;
//                if (pixelOffset == 7) pixelOffset = 6;

                context.drawGuiTexture(identifier1, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
            }
            if (j * 6 < iFoodLevel) {
                int pixelOffset = Math.max(0, partialHungerPips) + 1;
                if (pixelOffset == 1) pixelOffset = 2;
//                if (pixelOffset == 7) pixelOffset = 6;

                context.drawGuiTexture(identifier2, 9, 9, 8-pixelOffset, 0, l + (8-pixelOffset), k, pixelOffset+1, 9);
            }
        }
        RenderSystem.disableBlend();
        previousTime = Util.getMeasuringTimeNano();
//        {
//            int iShankScreenY = iScreenY;
//            int iShankTextureOffsetX = 16;
//            byte iBackgroundTextureOffsetX = 0;
//
//            if (player.isPotionActive(Potion.hunger)) {
//
//                iShankTextureOffsetX += 36;
//                iBackgroundTextureOffsetX = 13;
//            } else if (iTempCount < iSaturationPips >> 3) {
//
//                iBackgroundTextureOffsetX = 1;
//            }
//
//            if (iHungerPenalty > 0 && this.updateCounter % (iFoodLevel * 5 + 1) == 0) {
//
//                iShankScreenY = iScreenY + this.rand.nextInt(3) - 1;
//            } else if (this.foodBarShakeTimer > 0) {
//
//                int iShakeAmount = 1;
//
//                if (this.rand.nextInt(2) == 0) {
//                    iShakeAmount = -iShakeAmount;
//                }
//
//                iShankScreenY = iScreenY + iShakeAmount;
//            }
//
//            int iShankScreenX = iScreenX - iTempCount * 8 - 9;
//
//            drawTexturedModalRect(iShankScreenX, iShankScreenY, 16 + iBackgroundTextureOffsetX * 9, 27, 9, 9);
//
//            if (iTempCount == iSaturationPips >> 3) {
//                if (!player.isPotionActive(Potion.hunger)) {
//
//                    int iPartialPips = iSaturationPips % 8;
//
//                    if (iPartialPips != 0) {
//
//                        drawTexturedModalRect(iShankScreenX + 8 - iPartialPips, iShankScreenY, 33 - iPartialPips, 27, 1 + iPartialPips, 9);
//                    }
//                }
//            }
//
//            {
//                drawTexturedModalRect(iShankScreenX, iShankScreenY, iShankTextureOffsetX + 36, 27, 9, 9);
//            }
//            {
//                int iPartialPips = iFoodLevel % 6;
//
//                if (iPartialPips != 0) {
//                    drawTexturedModalRect(iShankScreenX + 7 - iPartialPips, iShankScreenY, iShankTextureOffsetX + 36 + 7 - iPartialPips, 27, 3 + iPartialPips, 9);
//                }
//            }
//        }
    }

}
