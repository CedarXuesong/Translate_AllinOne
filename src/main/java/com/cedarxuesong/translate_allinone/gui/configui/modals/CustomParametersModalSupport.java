package com.cedarxuesong.translate_allinone.gui.configui.modals;

import com.cedarxuesong.translate_allinone.gui.configui.model.ParameterListLocation;
import com.cedarxuesong.translate_allinone.gui.configui.model.ParameterTreeRow;
import com.cedarxuesong.translate_allinone.gui.configui.model.UiRect;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiModalSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.CustomParameterTreeSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderProfileSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.CustomParameterEntry;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class CustomParametersModalSupport {
    private CustomParametersModalSupport() {
    }

    public static String ensureSelection(List<CustomParameterEntry> draft, String selectedPath) {
        if (selectedPath != null && !selectedPath.isBlank() && CustomParameterTreeSupport.findByPath(draft, selectedPath) != null) {
            return selectedPath;
        }
        List<ParameterTreeRow> rows = CustomParameterTreeSupport.listRows(draft);
        return rows.isEmpty() ? "" : rows.get(0).path();
    }

    public static Fields render(
            int screenWidth,
            int screenHeight,
            List<CustomParameterEntry> modelSettingsCustomParametersDraft,
            Supplier<String> selectedPathSupplier,
            Consumer<String> selectedPathSetter,
            Translator translator,
            FloatingActionBlockAdder floatingActionBlockAdder,
            FloatingTextFieldAdder floatingTextFieldAdder,
            Consumer<Text> errorReporter,
            Runnable rebuildCustomFocus,
            Runnable onCancel,
            Runnable onDone,
            Style style
    ) {
        UiRect rect = ConfigUiModalSupport.customParametersModalRect(screenWidth, screenHeight);
        int contentTop = rect.y + 48;
        int contentBottom = rect.bottom() - 36;
        int leftX = rect.x + 20;
        int leftWidth = Math.max(220, rect.width / 2 - 28);
        int rightX = leftX + leftWidth + 10;
        int rightWidth = rect.right() - rightX - 20;

        List<ParameterTreeRow> rows = CustomParameterTreeSupport.listRows(modelSettingsCustomParametersDraft);
        String selectedPath = selectedPathSupplier.get();
        if (!rows.isEmpty() && CustomParameterTreeSupport.findByPath(modelSettingsCustomParametersDraft, selectedPath) == null) {
            selectedPathSetter.accept(rows.get(0).path());
        }

        floatingActionBlockAdder.add(
                leftX,
                contentTop,
                leftWidth,
                20,
                () -> translator.t("custom_params.columns"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );

        int rowY = contentTop + 24;
        int maxRowsBottom = contentBottom - 70;
        if (rows.isEmpty()) {
            floatingActionBlockAdder.add(
                    leftX,
                    rowY,
                    leftWidth,
                    20,
                    () -> translator.t("custom_params.empty"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
        } else {
            for (ParameterTreeRow row : rows) {
                if (rowY + 20 > maxRowsBottom) {
                    break;
                }

                boolean selected = row.path().equals(selectedPathSupplier.get());
                CustomParameterEntry entry = row.entry();
                String indent = "  ".repeat(Math.max(0, row.depth()));
                String typePrefix = entry.is_object ? "{ } " : "";
                String valueText = entry.is_object ? "" : " = " + ProviderProfileSupport.sanitizeText(entry.value);
                Text label = Text.literal(indent + typePrefix + ProviderProfileSupport.sanitizeText(entry.key) + valueText);

                floatingActionBlockAdder.add(
                        leftX,
                        rowY,
                        leftWidth,
                        20,
                        () -> label,
                        () -> {
                            selectedPathSetter.accept(row.path());
                            rebuildCustomFocus.run();
                        },
                        selected ? style.colorBlockSelected() : style.colorBlock(),
                        selected ? style.colorBlockSelectedHover() : style.colorBlockHover(),
                        selected ? style.colorTextAccent() : style.colorText(),
                        false
                );
                rowY += 22;
            }
        }

        floatingActionBlockAdder.add(
                leftX,
                contentBottom - 44,
                leftWidth,
                20,
                () -> translator.t("button.add_root_parameter"),
                () -> {
                    modelSettingsCustomParametersDraft.add(CustomParameterTreeSupport.createDefaultEntry());
                    selectedPathSetter.accept(Integer.toString(modelSettingsCustomParametersDraft.size() - 1));
                    rebuildCustomFocus.run();
                },
                style.colorBlockAccent(),
                style.colorBlockAccentHover(),
                style.colorText(),
                false
        );

        TextFieldWidget customParameterNameField = null;
        TextFieldWidget customParameterValueField = null;
        CustomParameterEntry selectedEntry = CustomParameterTreeSupport.findByPath(modelSettingsCustomParametersDraft, selectedPathSupplier.get());
        if (selectedEntry == null) {
            floatingActionBlockAdder.add(
                    rightX,
                    contentTop,
                    rightWidth,
                    20,
                    () -> translator.t("custom_params.select_hint"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
        } else {
            int editorY = contentTop;
            int labelWidth = 84;
            int valueX = rightX + labelWidth + 6;
            int valueWidth = Math.max(80, rightWidth - labelWidth - 6);

            floatingActionBlockAdder.add(
                    rightX,
                    editorY,
                    labelWidth,
                    20,
                    () -> translator.t("custom_params.name"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorText(),
                    false
            );
            customParameterNameField = floatingTextFieldAdder.add(
                    valueX,
                    editorY,
                    valueWidth,
                    128,
                    ProviderProfileSupport.sanitizeText(selectedEntry.key),
                    translator.t("custom_params.name"),
                    value -> selectedEntry.key = ProviderProfileSupport.sanitizeText(value),
                    true
            );
            editorY += 24;

            floatingActionBlockAdder.add(
                    rightX,
                    editorY,
                    labelWidth,
                    20,
                    () -> translator.t("custom_params.value"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorText(),
                    false
            );
            customParameterValueField = floatingTextFieldAdder.add(
                    valueX,
                    editorY,
                    valueWidth,
                    256,
                    ProviderProfileSupport.sanitizeText(selectedEntry.value),
                    translator.t("custom_params.value"),
                    value -> selectedEntry.value = ProviderProfileSupport.sanitizeText(value),
                    !selectedEntry.is_object
            );
            editorY += 24;

            floatingActionBlockAdder.add(
                    rightX,
                    editorY,
                    rightWidth,
                    20,
                    () -> translator.t("custom_params.detected_type", inferredValueTypeText(selectedEntry, translator)),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
            editorY += 24;

            floatingActionBlockAdder.add(
                    rightX,
                    editorY,
                    rightWidth,
                    20,
                    () -> selectedEntry.is_object ? translator.t("custom_params.type_object") : translator.t("custom_params.type_value"),
                    () -> {
                        if (selectedEntry.is_object && selectedEntry.children != null && !selectedEntry.children.isEmpty()) {
                            errorReporter.accept(translator.t("error.custom_params_object_has_children"));
                            return;
                        }
                        selectedEntry.is_object = !selectedEntry.is_object;
                        rebuildCustomFocus.run();
                    },
                    style.colorBlock(),
                    style.colorBlockHover(),
                    style.colorText(),
                    false
            );
            editorY += 24;

            int half = (rightWidth - 6) / 2;
            floatingActionBlockAdder.add(
                    rightX,
                    editorY,
                    half,
                    20,
                    () -> translator.t("button.add_sibling_parameter"),
                    () -> {
                        ParameterListLocation location = CustomParameterTreeSupport.findLocation(modelSettingsCustomParametersDraft, selectedPathSupplier.get());
                        if (location == null) {
                            errorReporter.accept(translator.t("error.custom_params_select_node"));
                            return;
                        }

                        location.list().add(location.index() + 1, CustomParameterTreeSupport.createDefaultEntry());
                        selectedPathSetter.accept(CustomParameterTreeSupport.composeChildPath(CustomParameterTreeSupport.parentPath(selectedPathSupplier.get()), location.index() + 1));
                        rebuildCustomFocus.run();
                    },
                    style.colorBlock(),
                    style.colorBlockHover(),
                    style.colorText(),
                    false
            );

            floatingActionBlockAdder.add(
                    rightX + half + 6,
                    editorY,
                    half,
                    20,
                    () -> translator.t("button.add_child_parameter"),
                    () -> {
                        selectedEntry.is_object = true;
                        if (selectedEntry.children == null) {
                            selectedEntry.children = new ArrayList<>();
                        }
                        selectedEntry.children.add(CustomParameterTreeSupport.createDefaultEntry());
                        selectedPathSetter.accept(CustomParameterTreeSupport.composeChildPath(selectedPathSupplier.get(), selectedEntry.children.size() - 1));
                        rebuildCustomFocus.run();
                    },
                    style.colorBlock(),
                    style.colorBlockHover(),
                    style.colorText(),
                    false
            );
            editorY += 24;

            floatingActionBlockAdder.add(
                    rightX,
                    editorY,
                    rightWidth,
                    20,
                    () -> translator.t("button.delete_parameter"),
                    () -> {
                        ParameterListLocation location = CustomParameterTreeSupport.findLocation(modelSettingsCustomParametersDraft, selectedPathSupplier.get());
                        if (location == null) {
                            errorReporter.accept(translator.t("error.custom_params_select_node"));
                            return;
                        }
                        if (location.index() < 0 || location.index() >= location.list().size()) {
                            errorReporter.accept(translator.t("error.custom_params_select_node"));
                            return;
                        }
                        location.list().remove(location.index());
                        selectedPathSetter.accept(ensureSelection(modelSettingsCustomParametersDraft, ""));
                        rebuildCustomFocus.run();
                    },
                    style.colorBlockDanger(),
                    style.colorBlockDangerHover(),
                    style.colorText(),
                    false
            );
        }

        int buttonsY = rect.bottom() - 28;
        int half = (rect.width - 48 - 6) / 2;
        int leftButtonX = rect.x + 24;
        int rightButtonX = leftButtonX + half + 6;
        floatingActionBlockAdder.add(
                leftButtonX,
                buttonsY,
                half,
                20,
                () -> translator.t("button.cancel"),
                onCancel,
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                true
        );
        floatingActionBlockAdder.add(
                rightButtonX,
                buttonsY,
                half,
                20,
                () -> translator.t("button.done"),
                onDone,
                style.colorBlockAccent(),
                style.colorBlockAccentHover(),
                style.colorText(),
                true
        );

        return new Fields(customParameterNameField, customParameterValueField);
    }

    private static String inferredValueTypeText(CustomParameterEntry entry, Translator translator) {
        if (entry == null || entry.is_object) {
            return translator.t("custom_params.type_object").getString();
        }
        Object parsed = CustomParameterTreeSupport.parseTypedValue(entry.value);
        if (parsed instanceof Boolean) {
            return translator.t("custom_params.value_type_bool").getString();
        }
        if (parsed instanceof Number) {
            return translator.t("custom_params.value_type_number").getString();
        }
        return translator.t("custom_params.value_type_string").getString();
    }

    public record Fields(TextFieldWidget nameField, TextFieldWidget valueField) {
    }

    @FunctionalInterface
    public interface Translator {
        Text t(String key, Object... args);
    }

    @FunctionalInterface
    public interface FloatingTextFieldAdder {
        TextFieldWidget add(
                int x,
                int y,
                int width,
                int maxLength,
                String initialValue,
                Text placeholder,
                Consumer<String> changed,
                boolean editable
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
            int colorBlockMuted,
            int colorText,
            int colorTextMuted,
            int colorBlock,
            int colorBlockHover,
            int colorBlockSelected,
            int colorBlockSelectedHover,
            int colorTextAccent,
            int colorBlockAccent,
            int colorBlockAccentHover,
            int colorBlockDanger,
            int colorBlockDangerHover
    ) {
    }
}
