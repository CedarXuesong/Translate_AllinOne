package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.model.ConfigSection;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigChromeActionsSupport {
    private ConfigChromeActionsSupport() {
    }

    public static void renderTopBarActions(
            int screenWidth,
            ActionBlockAdder actionBlockAdder,
            Translator translator,
            Runnable onReset,
            Runnable onCancel,
            Runnable onDone,
            Style style
    ) {
        int buttonY = 10;
        actionBlockAdder.add(
                screenWidth - 270,
                buttonY,
                80,
                20,
                () -> translator.t("button.reset"),
                onReset,
                style.colorBlockDanger(),
                style.colorBlockDangerHover(),
                style.colorText(),
                true
        );
        actionBlockAdder.add(
                screenWidth - 182,
                buttonY,
                80,
                20,
                () -> translator.t("button.cancel"),
                onCancel,
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                true
        );
        actionBlockAdder.add(
                screenWidth - 94,
                buttonY,
                80,
                20,
                () -> translator.t("button.done"),
                onDone,
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                true
        );
    }

    public static void renderSectionActions(
            ConfigSection selectedSection,
            int topBarHeight,
            int leftPanelWidth,
            ActionBlockAdder actionBlockAdder,
            Translator translator,
            Consumer<ConfigSection> onSelectSection,
            Style style
    ) {
        int x = 14;
        int y = topBarHeight + 12;
        for (ConfigSection section : ConfigSection.values()) {
            ConfigSection current = section;
            actionBlockAdder.add(
                    x,
                    y,
                    leftPanelWidth - 28,
                    20,
                    () -> {
                        String prefix = selectedSection == current ? "> " : "  ";
                        return Text.literal(prefix).append(translator.t(current.translationKey()));
                    },
                    () -> onSelectSection.accept(current),
                    style.colorBlock(),
                    style.colorBlockHover(),
                    style.colorText(),
                    false
            );
            y += 24;
        }
    }

    @FunctionalInterface
    public interface Translator {
        Text t(String key, Object... args);
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

    public record Style(
            int colorBlock,
            int colorBlockHover,
            int colorText,
            int colorBlockDanger,
            int colorBlockDangerHover
    ) {
    }
}
