package tetro48.system;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GranularHunger implements ModInitializer {
	public static final String MOD_ID = "granular-hunger";

	public static final Identifier EXHAUSTION_UPDATE_PACKET_ID = Identifier.of(MOD_ID, "exhaustion_update");
	public static final ComponentType<Integer> HUNGER_PIP_COMPONENT = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(GranularHunger.MOD_ID, "hunger_pips"),
			ComponentType.<Integer>builder().codec(Codec.INT).build()
	);
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		PayloadTypeRegistry.playS2C().register(ExhaustionUpdatePacket.ID, ExhaustionUpdatePacket.CODEC);
	}
}