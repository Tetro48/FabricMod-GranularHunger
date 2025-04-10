package tetro48.system.mixin.client;

import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tetro48.system.GranularHungerClient;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin {
    @Inject(method = "disconnect", at = @At("TAIL"))
    private void resetClientExhaustion(CallbackInfo ci) {
        GranularHungerClient.receivedExhaustionSinceLogin = false;
    }
}
