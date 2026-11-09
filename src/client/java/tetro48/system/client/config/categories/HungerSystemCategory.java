package tetro48.system.client.config.categories;

import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.ConfigType;
import net.azureaaron.dandelion.api.Option;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tetro48.system.GranularHunger;
import tetro48.system.HungerSystemBehaviorMode;
import tetro48.system.client.config.ConfigUtils;
import tetro48.system.configs.GranularHungerMainConfig;

import java.util.function.Function;

public class HungerSystemCategory {

	public static Function<HungerSystemBehaviorMode, Component> behaviorModeFormatter = (behaviorMode) -> switch (behaviorMode) {
		case BTW_MODE -> Component.translatable("granular_hunger.config.system.behavior_mode.btw_mode");
		case VANILLA_MODE -> Component.translatable("granular_hunger.config.system.behavior_mode.vanilla_mode");
	};
	public static ConfigCategory create(GranularHungerMainConfig defaults, GranularHungerMainConfig config) {
		return ConfigCategory.createBuilder()
				.id(Identifier.fromNamespaceAndPath(GranularHunger.MOD_ID, "system"))
				.name(Component.translatable("granular_hunger.config.system"))
				.description(Component.translatable("granular_hunger.config.system.description"))
				.option(Option.<HungerSystemBehaviorMode>createBuilder()
						.name(Component.translatable("granular_hunger.config.system.behavior_mode"))
						.description(Component.translatable("granular_hunger.config.system.behavior_mode.description"))
						.binding(defaults.hungerSystem.behaviorMode,
								() -> config.hungerSystem.behaviorMode,
								newBehavior -> config.hungerSystem.behaviorMode = newBehavior)
						.controller(ConfigUtils.createEnumController(behaviorModeFormatter))
						.build()
				)
				.option(Option.<Float>createBuilder()
						.name(Component.translatable("granular_hunger.config.system.hunger_ratio_regen", behaviorModeFormatter.apply(HungerSystemBehaviorMode.BTW_MODE)))
						.description(Component.translatable("granular_hunger.config.system.hunger_ratio_regen.description"))
						.binding(defaults.hungerSystem.hungerRatioRegen_BTWMode * 100f,
								() -> config.hungerSystem.hungerRatioRegen_BTWMode * 100f,
								ratio -> config.hungerSystem.hungerRatioRegen_BTWMode = ratio / 100f)
						.controller(ConfigUtils.createFloatSliderController(0, 100f, 0.1f))
						.build()
				)
				.option(Option.<Float>createBuilder()
						.name(Component.translatable("granular_hunger.config.system.hunger_ratio_regen", behaviorModeFormatter.apply(HungerSystemBehaviorMode.VANILLA_MODE)))
						.description(Component.translatable("granular_hunger.config.system.hunger_ratio_regen.description"))
						.binding(defaults.hungerSystem.hungerRatioRegen_VanillaMode * 100f,
								() -> config.hungerSystem.hungerRatioRegen_VanillaMode * 100f,
								ratio -> config.hungerSystem.hungerRatioRegen_VanillaMode = ratio / 100f)
						.controller(ConfigUtils.createFloatSliderController(0, 100f, 0.1f))
						.build()
				)
				.option(Option.<Float>createBuilder()
						.name(Component.translatable("granular_hunger.config.system.exhaustion_rate"))
						.description(Component.translatable("granular_hunger.config.system.exhaustion_rate.description"))
						.binding(defaults.hungerSystem.exhaustionRate,
								() -> config.hungerSystem.exhaustionRate,
								rate -> config.hungerSystem.exhaustionRate = rate)
						// yes that 0 is necessary, otherwise I absolutely lose my mind over that 1.4E-45 being in the configuration
						//   - A girlie who had to deal with this mess.
						.controller(ConfigUtils.createFloatController(0))
						.build()
				)

				.build();
	}
}
