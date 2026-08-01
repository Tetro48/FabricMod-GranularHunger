package tetro48.system.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tetro48.system.client.GranularHungerClient;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "disconnectFromWorld", at = @At("HEAD"))
	private void resetClientExhaustion(CallbackInfo ci) {
		GranularHungerClient.receivedExhaustionSinceLogin = false;
	}
}
