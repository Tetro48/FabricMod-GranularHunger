package tetro48.system.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import tetro48.system.ExhaustionUpdatePacket;
import tetro48.system.HungerBehaviorUpdatePacket;
import tetro48.system.GranularHunger;
import tetro48.system.HungerSystemBehaviorMode;
import tetro48.system.client.config.GranularHungerClientConfigManager;
import tetro48.system.configs.GranularHungerConfigManager;

public class GranularHungerClient implements ClientModInitializer {

	public static boolean receivedExhaustionSinceLogin;
	public static HungerSystemBehaviorMode hungerBehavior = HungerSystemBehaviorMode.BTW_MODE;
	public static double foodBarShakeTimer;
	public static double forcedShakeTime;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayNetworking.registerGlobalReceiver(ExhaustionUpdatePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				float newExhaustion = payload.exhaustion();
				if (receivedExhaustionSinceLogin) {
					foodBarShakeTimer += Math.max(0, Math.pow(newExhaustion * 5, 1.35d));
					if (newExhaustion > 0.001) forcedShakeTime = 0.1d;
				}
				else receivedExhaustionSinceLogin = true;
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(HungerBehaviorUpdatePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				try {
					hungerBehavior = HungerSystemBehaviorMode.valueOf(payload.enumIdentifier());
				} catch (IllegalArgumentException e) {
					GranularHunger.LOGGER.warn("Either you need to update Granular Hunger, or it's a packet with rogue data trying to crash the game.");
				}
			});
		});
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommands.literal(GranularHunger.MOD_ID).then(GranularHungerClientConfigManager.optionsLiteral("config")).then(GranularHungerClientConfigManager.optionsLiteral("options")))));
	}

	public static float getSaturationReduction(int foodLevel, int maxFoodLevel, int nutrition, float saturation) {
		if (GranularHungerClientConfigManager.get().hungerSystem.behaviorMode != HungerSystemBehaviorMode.BTW_MODE) {
			return 0;
		}
		return GranularHunger.getSaturationReduction(foodLevel, maxFoodLevel, nutrition, saturation);
	}
}