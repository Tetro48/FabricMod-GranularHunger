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

public class HungerSystemCategory {
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
						.controller(ConfigUtils.createEnumController(behaviorMode -> switch (behaviorMode) {
								case BTW_MODE -> Component.translatable("granular_hunger.config.system.behavior_mode.btw_mode");
								case VANILLA_MODE -> Component.translatable("granular_hunger.config.system.behavior_mode.vanilla_mode");
							}
						))
						.build()
				)

				.build();
	}
}
