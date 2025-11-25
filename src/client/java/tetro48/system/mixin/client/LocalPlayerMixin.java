package tetro48.system.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
	public LocalPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "hasEnoughFoodToStartSprinting", at = @At("RETURN"), cancellable = true)
	private void noSprintUnder3Shanks(CallbackInfoReturnable<Boolean> cir) {
		if (this.getFoodData().getFoodLevel() <= 18 && cir.getReturnValue()) {
			cir.setReturnValue(false);
		}
	}
}
