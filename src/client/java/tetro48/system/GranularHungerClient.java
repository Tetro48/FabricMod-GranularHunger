package tetro48.system;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class GranularHungerClient implements ClientModInitializer {

	public static boolean receivedExhaustionSinceLogin;
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
	}
}