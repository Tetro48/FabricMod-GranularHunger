package tetro48.system.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import tetro48.system.ExhaustionUpdatePacket;

public class GranularHungerClient implements ClientModInitializer {

	public static boolean receivedExhaustionSinceLogin;
	public static float foodBarShakeTimer;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayNetworking.registerGlobalReceiver(ExhaustionUpdatePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				float newExhaustion = payload.exhaustion();
				if (receivedExhaustionSinceLogin)
					foodBarShakeTimer += (float) Math.max(0, Math.pow(newExhaustion, 1.2d)) * 10;
				else receivedExhaustionSinceLogin = true;
			});
		});
	}
}