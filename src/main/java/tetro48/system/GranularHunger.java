package tetro48.system;

import com.mojang.serialization.Codec;
import net.azureaaron.dandelion.api.ConfigManager;
import net.azureaaron.dandelion.api.ConfigType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tetro48.system.configs.GranularHungerConfigManager;
import tetro48.system.configs.GranularHungerMainConfig;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class GranularHunger implements ModInitializer {
	public static final int CONFIG_VERSION = 1;
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("granular_hunger.json");
	public static final ConfigManager<GranularHungerMainConfig> CONFIG_MANAGER = ConfigManager.create(GranularHungerMainConfig.class, CONFIG_FILE, UnaryOperator.identity());


	private static final long[] ticksToHealOnDifficulty = {160, 240, 400, 400};

	public static final String MOD_ID = "granular_hunger";

	public static final Identifier EXHAUSTION_UPDATE_PACKET_ID = Identifier.fromNamespaceAndPath(MOD_ID, "exhaustion_update");
	public static final Identifier HUNGER_BEHAVIOR_UPDATE_PACKET_ID = Identifier.fromNamespaceAndPath(MOD_ID, "hunger_behavior");
	public static final DataComponentType<Integer> HUNGER_PIP_COMPONENT = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(GranularHunger.MOD_ID, "hunger_pips"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).build()
	);

	public static final Holder<Attribute> HUNGER_COST_MULTIPLIER_ATTRIBUTE = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE,
			Identifier.fromNamespaceAndPath(MOD_ID, "hunger_cost"),
			new RangedAttribute("attribute.name." + MOD_ID + ".hunger_cost", 1d, 0d, Double.POSITIVE_INFINITY)
					.setSentiment(Attribute.Sentiment.NEGATIVE));
	public static final Holder<Attribute> MAX_HUNGER_ATTRIBUTE = Registry.registerForHolder(
			BuiltInRegistries.ATTRIBUTE,
			Identifier.fromNamespaceAndPath(MOD_ID, "max_hunger"),
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

		GranularHungerConfigManager.init();
		LOGGER.info("Hello Fabric world!");
		PayloadTypeRegistry.clientboundPlay().register(ExhaustionUpdatePacket.ID, ExhaustionUpdatePacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HungerBehaviorUpdatePacket.ID, HungerBehaviorUpdatePacket.CODEC);
	}

	public static long getTicksUntilHeal(ServerLevel level) {
		return ticksToHealOnDifficulty[level.getDifficulty().getId()];
	}

	public static float getSaturationReduction(int foodLevel, int maxFoodLevel, int nutrition, float saturation) {
		if (GranularHungerConfigManager.get().hungerSystem.behaviorMode != HungerSystemBehaviorMode.BTW_MODE) {
			return foodLevel;
		}
		int excess = Math.max(foodLevel + nutrition - maxFoodLevel, 0);
		return saturation * (nutrition-excess)/(float)nutrition;
	}
}