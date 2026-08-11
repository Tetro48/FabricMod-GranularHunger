package tetro48.system.client.config;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.azureaaron.dandelion.api.ConfigManager;
import net.azureaaron.dandelion.api.DandelionConfigScreen;
import net.azureaaron.dandelion.api.PlatformLinks;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.function.Consumers;
import tetro48.system.GranularHunger;
import tetro48.system.configs.GranularHungerMainConfig;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class GranularHungerConfigManager {
	public static final int CONFIG_VERSION = 1;
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skyblocker.json");
	private static final ConfigManager<GranularHungerMainConfig> CONFIG_MANAGER = ConfigManager.create(GranularHungerMainConfig.class, CONFIG_FILE, UnaryOperator.identity());

	public static GranularHungerMainConfig get() {
		return CONFIG_MANAGER.instance();
	}

	/**
	 * This method is caller sensitive and can only be called by the mod initializer,
	 * this is enforced.
	 */
	public static void init() {
		if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != GranularHungerMainConfig.class) {
			throw new RuntimeException("Granular Hunger: Called config init from an illegal place!");
		}

		CONFIG_MANAGER.load();
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommands.literal(GranularHunger.MOD_ID).then(optionsLiteral("config")).then(optionsLiteral("options")))));
	}

	/**
	 * Executes the given {@code action} to update fields in the config, then saves the changes.
	 */
	public static void update(Consumer<GranularHungerMainConfig> action) {
		action.accept(get());
		CONFIG_MANAGER.save();
	}

	public static Screen createGUI(Screen parent) {
		return DandelionConfigScreen.create(CONFIG_MANAGER, (defaults, config, builder) -> builder
				.title(Component.literal("Granular Hunger"))
				.category(GeneralCategory.create(defaults, config))
				.category(UIAndVisualsCategory.create(defaults, config))
				.platformLinks(PlatformLinks.createBuilder()
						.link(Component.literal("GitHub"), PlatformLinks.GITHUB_ICON, "https://github.com/SkyblockerMod/Skyblocker")
						.link(Component.literal("Modrinth"), PlatformLinks.MODRINTH_ICON, "https://modrinth.com/mod/skyblocker-liap")
						.build())
		).generateScreen(parent, get().misc.configBackend);



	}

	/**
	 * Registers an options command with the given name. Used for registering both options and config as valid commands.
	 *
	 * @param name the name of the command node
	 * @return the command builder
	 */
	private static LiteralArgumentBuilder<FabricClientCommandSource> optionsLiteral(String name) {
		// Don't immediately open the next screen as it will be closed by ChatScreen right after this command is executed
		return ClientCommands.literal(name).executes(Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(createGUI(null))));
	}
}
