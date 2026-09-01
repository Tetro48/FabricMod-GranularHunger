package tetro48.system.client.config;

import net.azureaaron.dandelion.api.controllers.BooleanController;
import net.azureaaron.dandelion.api.controllers.ColourController;
import net.azureaaron.dandelion.api.controllers.EnumController;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class ConfigUtils {
	public static final Function<ChatFormatting, Component> FORMATTING_FORMATTER = formatting -> Component.literal(StringUtils.capitalize(formatting.name().replaceAll("_", " ")));

	public static BooleanController createBooleanController() {
		return BooleanController.createBuilder()
				.coloured(true)
				.booleanStyle(BooleanController.BooleanStyle.YES_NO)
				.build();
	}

	public static ColourController createColourController(boolean hasAlpha) {
		return ColourController.createBuilder()
				.hasAlpha(hasAlpha)
				.build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumController() {
		return (EnumController<T>) EnumController.createBuilder().build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumController(Function<T, Component> formatter) {
		return (EnumController<T>) EnumController.createBuilder().formatter(Function.class.cast(formatter)).build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumDropdownController(Function<T, Component> formatter) {
		return (EnumController<T>) EnumController.createBuilder().dropdown(true).formatter(Function.class.cast(formatter)).build();
	}
}