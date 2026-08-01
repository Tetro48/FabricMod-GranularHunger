package tetro48.system.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
//	public LocalPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
//		super(world, pos, yaw, gameProfile);
//	}
//
//	@Inject(method = "canStartSprinting", at = @At("RETURN"), cancellable = true)
//	private void noSprintUnder3Shanks(CallbackInfoReturnable<Boolean> cir) {
//		if (this.getFoodData().getFoodLevel() <= 18 && cir.getReturnValue()) {
//			cir.setReturnValue(false);
//		}
//	}
}
