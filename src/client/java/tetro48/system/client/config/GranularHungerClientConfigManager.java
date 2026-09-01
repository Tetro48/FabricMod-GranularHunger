package tetro48.system.client.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.azureaaron.dandelion.api.DandelionConfigScreen;
import net.azureaaron.dandelion.api.PlatformLinks;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tetro48.system.GranularHunger;
import tetro48.system.client.GranularHungerClient;
import tetro48.system.client.config.categories.HungerDisplayCategory;
import tetro48.system.client.config.categories.HungerSystemCategory;
import tetro48.system.configs.GranularHungerMainConfig;

import java.util.function.Consumer;

public class GranularHungerClientConfigManager {



	public static GranularHungerMainConfig get() {
		return GranularHunger.CONFIG_MANAGER.instance();
	}

	public static Screen createGUI(Screen parent) {
		return DandelionConfigScreen.create(GranularHunger.CONFIG_MANAGER, (defaults, config, builder) -> builder
				.title(Component.literal("Granular Hunger"))
				.category(HungerDisplayCategory.create(defaults, config))
				.categoryIf(!GranularHungerClient.receivedExhaustionSinceLogin ||
						Minecraft.getInstance().hasSingleplayerServer(), HungerSystemCategory.create(defaults, config))
				.platformLinks(PlatformLinks.createBuilder()
						.link(Component.literal("GitHub"), PlatformLinks.GITHUB_ICON, "https://github.com/Tetro48/FabricMod-GranularHunger")
						.link(Component.literal("Modrinth"), PlatformLinks.MODRINTH_ICON, "https://modrinth.com/mod/granular-hunger")
						.build())
		).generateScreen(parent, get().hungerDisplay.configBackend);
	}

	/**
	 * Registers an options command with the given name. Used for registering both options and config as valid commands.
	 *
	 * @param name the name of the command node
	 * @return the command builder
	 */
	public static LiteralArgumentBuilder<FabricClientCommandSource> optionsLiteral(String name) {
		// Don't immediately open the next screen as it will be closed by ChatScreen right after this command is executed
		return ClientCommands.literal(name).executes(_ -> {Minecraft.getInstance().schedule(() ->
			Minecraft.getInstance().gui.setScreen(createGUI(null)));
		return Command.SINGLE_SUCCESS;});
	}
}
