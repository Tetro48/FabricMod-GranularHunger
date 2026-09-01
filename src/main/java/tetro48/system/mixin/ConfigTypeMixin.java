package tetro48.system.mixin;

import net.azureaaron.dandelion.api.ConfigType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tetro48.system.GranularHunger;

///
/// Dandelion is not made for the fact that Granular Hunger will also use it on the server-side. So, Granular Hunger will forcefully use the internal name.
///

@Mixin(ConfigType.class)
public class ConfigTypeMixin {
	@Inject(method = "toString", at = @At("HEAD"), cancellable = true)
	private void fixString(CallbackInfoReturnable<String> cir) {
		try {
			Class.forName("net.minecraft.client.resources.language.I18n");
		} catch (ClassNotFoundException e) {
			GranularHunger.LOGGER.info("Caught a server-side only exception, forcing the internal ConfigType name for server-side operations.");
			cir.setReturnValue(((ConfigType)(Object)this).name());
		}
	}
}
