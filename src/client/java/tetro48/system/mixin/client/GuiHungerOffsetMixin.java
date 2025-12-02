package tetro48.system.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import tetro48.system.GranularHunger;

@Mixin(Gui.class)
public abstract class GuiHungerOffsetMixin {
	@Shadow @Nullable protected abstract Player getCameraPlayer();

	@ModifyArg(method = "renderPlayerHealth", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V"))
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
}
