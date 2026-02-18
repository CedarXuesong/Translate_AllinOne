package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.model.ConfigSection;
import com.cedarxuesong.translate_allinone.gui.configui.model.RouteSlot;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.InputBindingConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.ModConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class ConfigSectionContentSupport {
    private static final int ROW_STEP = 24;
    private static final int MIN_CONTENT_HEIGHT = 20;
    private static final int SLIDER_STEP = 30;
    private static final int SECTION_TOP_MARGIN = 16;
    private static final int GROUP_GAP = 34;
    private static final int GROUP_PADDING_TOP = 18;
    private static final int GROUP_PADDING_BOTTOM = 8;
    private static final int GROUP_PADDING_SIDE = 6;

    private ConfigSectionContentSupport() {
    }

    public static int render(
            ConfigSection selectedSection,
            ModConfig config,
            int x,
            int y,
            int width,
            int viewportHeight,
            Translator translator,
            GroupBoxAdder groupBoxAdder,
            ToggleAdder toggleAdder,
            IntSliderAdder sliderAdder,
            ActionAdder actionAdder,
            TextFieldRowAdder textFieldRowAdder,
            BindingLabelProvider bindingLabelProvider,
            HotkeyAction hotkeyStartBinding,
            HotkeyAction hotkeyClearBinding,
            HotkeyAction hotkeyCycleMode,
            RouteSelectorAdder routeSelectorAdder,
            ProviderSectionAdder providerSectionAdder
    ) {
        if (selectedSection != ConfigSection.PROVIDERS) {
            y += SECTION_TOP_MARGIN;
        }

        switch (selectedSection) {
            case CHAT_OUTPUT -> {
                ChatTranslateConfig.ChatOutputTranslateConfig output = config.chatTranslate.output;
                int basicStart = y;
                toggleAdder.add(x, y, width, translator.t("label.enabled"), () -> output.enabled, value -> output.enabled = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.auto_translate"), () -> output.auto_translate, value -> output.auto_translate = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.streaming"), () -> output.streaming_response, value -> output.streaming_response = value);
                y += ROW_STEP;
                textFieldRowAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.target_language"),
                        48,
                        output.target_language,
                        translator.t("placeholder.target_language"),
                        value -> output.target_language = sanitizeLanguage(value),
                        true
                );
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.basic"), x, width, basicStart, y);

                y += GROUP_GAP;
                int routeStart = y;
                routeSelectorAdder.add(config.providerManager, RouteSlot.CHAT_OUTPUT, x, y, width);
                addGroupBox(groupBoxAdder, translator.t("group.route"), x, width, routeStart, routeStart + ROW_STEP);
                return routeStart + ROW_STEP;
            }
            case CHAT_INPUT -> {
                ChatTranslateConfig.ChatInputTranslateConfig input = config.chatTranslate.input;
                if (input.keybinding == null) {
                    input.keybinding = new InputBindingConfig();
                }

                int basicStart = y;
                toggleAdder.add(x, y, width, translator.t("label.enabled"), () -> input.enabled, value -> input.enabled = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.streaming"), () -> input.streaming_response, value -> input.streaming_response = value);
                y += ROW_STEP;
                textFieldRowAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.target_language"),
                        48,
                        input.target_language,
                        translator.t("placeholder.target_language"),
                        value -> input.target_language = sanitizeLanguage(value),
                        true
                );
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.basic"), x, width, basicStart, y);

                y += GROUP_GAP;
                int hotkeyStart = y;
                actionAdder.add(x, y, width, bindingLabelProvider.label(HotkeyTarget.CHAT_INPUT, input.keybinding), () -> hotkeyStartBinding.handle(HotkeyTarget.CHAT_INPUT));
                y += ROW_STEP;
                actionAdder.add(x, y, width, translator.t("button.hotkey_clear"), () -> hotkeyClearBinding.handle(HotkeyTarget.CHAT_INPUT));
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.hotkey"), x, width, hotkeyStart, y);

                y += GROUP_GAP;
                int routeStart = y;
                routeSelectorAdder.add(config.providerManager, RouteSlot.CHAT_INPUT, x, y, width);
                addGroupBox(groupBoxAdder, translator.t("group.route"), x, width, routeStart, routeStart + ROW_STEP);
                return routeStart + ROW_STEP;
            }
            case ITEM -> {
                ItemTranslateConfig item = config.itemTranslate;
                if (item.keybinding == null) {
                    item.keybinding = new ItemTranslateConfig.KeybindingConfig();
                }
                if (item.keybinding.binding == null) {
                    item.keybinding.binding = new InputBindingConfig();
                }

                int basicStart = y;
                toggleAdder.add(x, y, width, translator.t("label.enabled"), () -> item.enabled, value -> item.enabled = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.translate_item_name"), () -> item.enabled_translate_item_custom_name, value -> item.enabled_translate_item_custom_name = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.translate_item_lore"), () -> item.enabled_translate_item_lore, value -> item.enabled_translate_item_lore = value);
                y += ROW_STEP;
                textFieldRowAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.target_language"),
                        48,
                        item.target_language,
                        translator.t("placeholder.target_language"),
                        value -> item.target_language = sanitizeLanguage(value),
                        true
                );
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.basic"), x, width, basicStart, y);

                y += GROUP_GAP;
                int hotkeyStart = y;

                actionAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.hotkey_mode", modeText(translator, item.keybinding.mode.name())),
                        () -> hotkeyCycleMode.handle(HotkeyTarget.ITEM)
                );
                y += ROW_STEP;
                actionAdder.add(x, y, width, bindingLabelProvider.label(HotkeyTarget.ITEM, item.keybinding.binding), () -> hotkeyStartBinding.handle(HotkeyTarget.ITEM));
                y += ROW_STEP;
                actionAdder.add(x, y, width, translator.t("button.hotkey_clear"), () -> hotkeyClearBinding.handle(HotkeyTarget.ITEM));
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.hotkey"), x, width, hotkeyStart, y);

                y += GROUP_GAP;
                int performanceStart = y;

                sliderAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.item_threads"),
                        1,
                        16,
                        () -> item.max_concurrent_requests,
                        value -> item.max_concurrent_requests = value
                );
                y += SLIDER_STEP;

                sliderAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.item_batch_size"),
                        1,
                        64,
                        () -> item.max_batch_size,
                        value -> item.max_batch_size = value
                );
                y += SLIDER_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.performance"), x, width, performanceStart, y);

                y += GROUP_GAP;
                int routeStart = y;
                routeSelectorAdder.add(config.providerManager, RouteSlot.ITEM, x, y, width);
                addGroupBox(groupBoxAdder, translator.t("group.route"), x, width, routeStart, routeStart + ROW_STEP);
                return routeStart + ROW_STEP;
            }
            case SCOREBOARD -> {
                ScoreboardConfig scoreboard = config.scoreboardTranslate;
                if (scoreboard.keybinding == null) {
                    scoreboard.keybinding = new ScoreboardConfig.KeybindingConfig();
                }
                if (scoreboard.keybinding.binding == null) {
                    scoreboard.keybinding.binding = new InputBindingConfig();
                }

                int basicStart = y;
                toggleAdder.add(x, y, width, translator.t("label.enabled"), () -> scoreboard.enabled, value -> scoreboard.enabled = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.translate_prefix_suffix"), () -> scoreboard.enabled_translate_prefix_and_suffix_name, value -> scoreboard.enabled_translate_prefix_and_suffix_name = value);
                y += ROW_STEP;
                toggleAdder.add(x, y, width, translator.t("label.translate_player_name"), () -> scoreboard.enabled_translate_player_name, value -> scoreboard.enabled_translate_player_name = value);
                y += ROW_STEP;
                textFieldRowAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.target_language"),
                        48,
                        scoreboard.target_language,
                        translator.t("placeholder.target_language"),
                        value -> scoreboard.target_language = sanitizeLanguage(value),
                        true
                );
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.basic"), x, width, basicStart, y);

                y += GROUP_GAP;
                int hotkeyStart = y;
                actionAdder.add(
                        x,
                        y,
                        width,
                        translator.t("label.hotkey_mode", modeText(translator, scoreboard.keybinding.mode.name())),
                        () -> hotkeyCycleMode.handle(HotkeyTarget.SCOREBOARD)
                );
                y += ROW_STEP;
                actionAdder.add(x, y, width, bindingLabelProvider.label(HotkeyTarget.SCOREBOARD, scoreboard.keybinding.binding), () -> hotkeyStartBinding.handle(HotkeyTarget.SCOREBOARD));
                y += ROW_STEP;
                actionAdder.add(x, y, width, translator.t("button.hotkey_clear"), () -> hotkeyClearBinding.handle(HotkeyTarget.SCOREBOARD));
                y += ROW_STEP;
                addGroupBox(groupBoxAdder, translator.t("group.hotkey"), x, width, hotkeyStart, y);

                y += GROUP_GAP;
                int routeStart = y;
                routeSelectorAdder.add(config.providerManager, RouteSlot.SCOREBOARD, x, y, width);
                addGroupBox(groupBoxAdder, translator.t("group.route"), x, width, routeStart, routeStart + ROW_STEP);
                return routeStart + ROW_STEP;
            }
            case PROVIDERS -> {
                return providerSectionAdder.add(config.providerManager, x, y, width, viewportHeight);
            }
        }

        return y;
    }

    private static void addGroupBox(
            GroupBoxAdder groupBoxAdder,
            Text title,
            int x,
            int width,
            int contentStartY,
            int contentEndY
    ) {
        int contentBottom = Math.max(contentStartY + MIN_CONTENT_HEIGHT, contentEndY);
        int groupX = x - GROUP_PADDING_SIDE;
        int groupY = contentStartY - GROUP_PADDING_TOP;
        int groupWidth = width + GROUP_PADDING_SIDE * 2;
        int groupHeight = (contentBottom - contentStartY) + GROUP_PADDING_TOP + GROUP_PADDING_BOTTOM;
        groupBoxAdder.add(groupX, groupY, groupWidth, groupHeight, title);
    }

    private static Text modeText(Translator translator, String modeName) {
        return switch (modeName) {
            case "HOLD_TO_TRANSLATE" -> translator.t("state.hold_to_translate");
            case "HOLD_TO_SEE_ORIGINAL" -> translator.t("state.hold_to_see_original");
            default -> translator.t("state.disabled");
        };
    }

    private static String sanitizeLanguage(String language) {
        if (language == null) {
            return "";
        }
        return language.trim();
    }

    @FunctionalInterface
    public interface Translator {
        Text t(String key, Object... args);
    }

    @FunctionalInterface
    public interface GroupBoxAdder {
        void add(int x, int y, int width, int height, Text title);
    }

    @FunctionalInterface
    public interface ToggleAdder {
        void add(int x, int y, int width, Text label, BooleanSupplier getter, Consumer<Boolean> setter);
    }

    @FunctionalInterface
    public interface IntSliderAdder {
        void add(int x, int y, int width, Text label, int min, int max, IntSupplier getter, IntConsumer setter);
    }

    @FunctionalInterface
    public interface ActionAdder {
        void add(int x, int y, int width, Text label, Runnable action);
    }

    @FunctionalInterface
    public interface TextFieldRowAdder {
        void add(
                int x,
                int y,
                int width,
                Text label,
                int maxLength,
                String initialValue,
                Text placeholder,
                Consumer<String> changed,
                boolean editable
        );
    }

    @FunctionalInterface
    public interface BindingLabelProvider {
        Text label(HotkeyTarget target, InputBindingConfig binding);
    }

    @FunctionalInterface
    public interface HotkeyAction {
        void handle(HotkeyTarget target);
    }

    @FunctionalInterface
    public interface RouteSelectorAdder {
        void add(ProviderManagerConfig manager, RouteSlot routeSlot, int x, int y, int width);
    }

    @FunctionalInterface
    public interface ProviderSectionAdder {
        int add(ProviderManagerConfig providerManager, int x, int y, int width, int viewportHeight);
    }

    public enum HotkeyTarget {
        CHAT_INPUT,
        ITEM,
        SCOREBOARD
    }
}
