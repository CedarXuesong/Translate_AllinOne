package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.model.RouteModelOption;
import com.cedarxuesong.translate_allinone.gui.configui.model.RouteSlot;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public final class RouteModelSelectorSectionSupport {
    private RouteModelSelectorSectionSupport() {
    }

    public static void render(
            ProviderManagerConfig manager,
            RouteSlot routeSlot,
            int x,
            int y,
            int width,
            boolean dropdownOpen,
            Translator translator,
            ActionBlockAdder actionBlockAdder,
            FloatingActionBlockAdder floatingActionBlockAdder,
            Runnable onToggleDropdown,
            RouteSelectionHandler onSelected,
            Style style
    ) {
        String selectedRouteKey = RouteModelSectionSupport.getRouteKey(manager, routeSlot);

        actionBlockAdder.add(
                x,
                y,
                width,
                20,
                () -> translator.t(
                        "label.route_model",
                        RouteModelSectionSupport.describeRouteModel(
                                selectedRouteKey,
                                manager,
                                translator.t("route_model.none"),
                                key -> translator.t("provider.missing", key)
                        )
                ),
                onToggleDropdown,
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                false
        );

        if (!dropdownOpen) {
            return;
        }

        int dropdownX = x;
        int optionY = y + 22;
        int dropdownWidth = width;
        for (RouteModelOption option : RouteModelSectionSupport.buildRouteModelOptions(manager, translator.t("route_model.none"))) {
            boolean selected = option.routeKey().equals(selectedRouteKey == null ? "" : selectedRouteKey);
            int color = selected ? style.colorBlockAccent() : style.colorBlock();
            int hoverColor = selected ? style.colorBlockAccentHover() : style.colorBlockHover();
            int textColor = selected ? style.colorTextAccent() : style.colorText();
            floatingActionBlockAdder.add(
                    dropdownX,
                    optionY,
                    dropdownWidth,
                    20,
                    () -> option.displayLabel(),
                    () -> onSelected.onSelected(option.routeKey(), option.displayLabel()),
                    color,
                    hoverColor,
                    textColor,
                    false
            );
            optionY += 22;
        }
    }

    @FunctionalInterface
    public interface Translator {
        Text t(String key, Object... args);
    }

    @FunctionalInterface
    public interface RouteSelectionHandler {
        void onSelected(String routeKey, Text displayLabel);
    }

    @FunctionalInterface
    public interface ActionBlockAdder {
        void add(
                int x,
                int y,
                int width,
                int height,
                Supplier<Text> labelSupplier,
                Runnable action,
                int color,
                int hoverColor,
                int textColor,
                boolean centered
        );
    }

    @FunctionalInterface
    public interface FloatingActionBlockAdder {
        void add(
                int x,
                int y,
                int width,
                int height,
                Supplier<Text> labelSupplier,
                Runnable action,
                int color,
                int hoverColor,
                int textColor,
                boolean centered
        );
    }

    public record Style(
            int colorBlock,
            int colorBlockHover,
            int colorBlockAccent,
            int colorBlockAccentHover,
            int colorText,
            int colorTextAccent
    ) {
    }
}
