package tetro48.system.mixin;

import net.minecraft.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {
	@ModifyArg(method = "tryEat", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canConsume(Z)Z"))
	private static boolean canEatCakeEvenIfFull(boolean ignoreHunger) {
		return true;
	}
	@ModifyArgs(method = "tryEat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
	private static void makeCakeABTWDessert(Args args) {
		args.set(0, 4);
		args.set(1, 2f);
	}
}
