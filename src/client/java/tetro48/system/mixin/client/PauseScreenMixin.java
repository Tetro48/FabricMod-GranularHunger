package tetro48.system.mixin.client;

import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tetro48.system.GranularHungerClient;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
	@Inject(method = "onDisconnect", at = @At("TAIL"))
	private void resetClientExhaustion(CallbackInfo ci) {
		GranularHungerClient.receivedExhaustionSinceLogin = false;
	}
}
