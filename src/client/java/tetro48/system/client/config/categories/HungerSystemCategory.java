package tetro48.system.client.config.categories;

import net.azureaaron.dandelion.api.ConfigCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tetro48.system.GranularHunger;
import tetro48.system.configs.GranularHungerMainConfig;

public class HungerSystemCategory {
	public static ConfigCategory create(GranularHungerMainConfig defaults, GranularHungerMainConfig config) {
		return ConfigCategory.createBuilder()
				.id(Identifier.fromNamespaceAndPath(GranularHunger.MOD_ID, "general"))
				.name(Component.literal("Granular Hunger"))
				.build();
	}
}
