package tetro48.system.client.config.categories;

import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.ConfigType;
import net.azureaaron.dandelion.api.Option;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tetro48.system.GranularHunger;
import tetro48.system.client.config.ConfigUtils;
import tetro48.system.configs.GranularHungerMainConfig;

import java.awt.*;

public class HungerDisplayCategory {
	public static ConfigCategory create(GranularHungerMainConfig defaults, GranularHungerMainConfig config) {
		return ConfigCategory.createBuilder()
				.id(Identifier.fromNamespaceAndPath(GranularHunger.MOD_ID, "display"))
				.name(Component.translatable("granular_hunger.config.visuals"))
				.description(Component.translatable("granular_hunger.config.visuals.description"))
				.option(Option.<Color>createBuilder()
						.name(Component.translatable("granular_hunger.config.visuals.fat_color"))
						.binding(defaults.hungerDisplay.fatColor,
								() -> config.hungerDisplay.fatColor,
								color -> config.hungerDisplay.fatColor = color)
						.controller(ConfigUtils.createColourController(false))
						.build()
				)
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("granular_hunger.config.visuals.fill_overlay"))
						.description(Component.translatable("granular_hunger.config.visuals.fill_overlay.description"))
						.binding(defaults.hungerDisplay.displayOverlay,
								() -> config.hungerDisplay.displayOverlay,
								showOverlay -> config.hungerDisplay.displayOverlay = showOverlay)
						.controller(ConfigUtils.createBooleanController())
						.build()
				)

				.option(Option.<ConfigType>createBuilder()
						.name(Component.translatable("granular_hunger.config.visuals.config_backend"))
						.binding(defaults.hungerDisplay.configBackend,
								() -> config.hungerDisplay.configBackend,
								newValue -> config.hungerDisplay.configBackend = newValue)
						.controller(ConfigUtils.createEnumController())
						.build()
				)
				.build();
	}
}
