package tetro48.system.configs;

import tetro48.system.GranularHunger;

import java.util.function.Consumer;

public class GranularHungerConfigManager {
	public static GranularHungerMainConfig get() {
		return GranularHunger.CONFIG_MANAGER.instance();
	}

	/**
	 * This method is caller sensitive and can only be called by the mod initializer,
	 * this is enforced.
	 */
	public static void init() {
		if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != GranularHunger.class) {
			throw new RuntimeException("Granular Hunger: Called config init from an illegal place!");
		}

		GranularHunger.CONFIG_MANAGER.load();
	}

	/**
	 * Executes the given {@code action} to update fields in the config, then saves the changes.
	 */
	public static void update(Consumer<GranularHungerMainConfig> action) {
		action.accept(get());
		GranularHunger.CONFIG_MANAGER.save();
	}
}
