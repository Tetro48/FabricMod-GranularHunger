package tetro48.system;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GranularHunger implements ModInitializer {
	public static final String MOD_ID = "granular_hunger";

	public static final ResourceLocation EXHAUSTION_UPDATE_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "exhaustion_update");
	public static final DataComponentType<Integer> HUNGER_PIP_COMPONENT = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ResourceLocation.fromNamespaceAndPath(GranularHunger.MOD_ID, "hunger_pips"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).build()
	);

	public static final Holder<Attribute> HUNGER_COST_MULTIPLIER_ATTRIBUTE = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "hunger_cost"),
			new RangedAttribute("attribute.name." + MOD_ID + ".hunger_cost", 1d, 0d, Double.POSITIVE_INFINITY)
					.setSentiment(Attribute.Sentiment.NEGATIVE));
	public static final Holder<Attribute> MAX_HUNGER_ATTRIBUTE = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE,
			ResourceLocation.fromNamespaceAndPath(MOD_ID, "max_hunger"),
			new RangedAttribute("attribute.name." + MOD_ID + ".max_hunger", 60d, 0d, Double.POSITIVE_INFINITY)
					.setSyncable(true));
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