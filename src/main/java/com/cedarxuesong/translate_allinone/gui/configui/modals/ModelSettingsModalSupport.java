package com.cedarxuesong.translate_allinone.gui.configui.modals;

import com.cedarxuesong.translate_allinone.gui.configui.controls.CheckboxBlock;
import com.cedarxuesong.translate_allinone.gui.configui.model.UiRect;
import com.cedarxuesong.translate_allinone.gui.configui.render.ConfigUiModalSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModelSettingsModalSupport {
    private ModelSettingsModalSupport() {
    }

    public static TextFieldWidget render(
            ApiProviderProfile profile,
            int screenWidth,
            int screenHeight,
            String modelSettingsDraft,
            String modelSettingsTemperatureDraft,
            String modelSettingsKeepAliveDraft,
            String modelSettingsSystemPromptSuffixDraft,
            int customParameterCount,
            boolean modelSettingsSupportsSystemDraft,
            boolean modelSettingsInjectPromptIntoUserDraft,
            boolean modelSettingsStructuredOutputDraft,
            boolean modelSettingsSetDefault,
            Translator translator,
            FloatingActionBlockAdder floatingActionBlockAdder,
            FloatingCheckboxAdder floatingCheckboxAdder,
            FloatingTextFieldAdder floatingTextFieldAdder,
            Consumer<String> onModelIdChanged,
            Consumer<String> onTemperatureChanged,
            Consumer<String> onKeepAliveChanged,
            Consumer<String> onSystemPromptSuffixChanged,
            Runnable onEditCustomParameters,
            Consumer<Boolean> onSupportsSystemChanged,
            Consumer<Boolean> onInjectPromptIntoUserChanged,
            Consumer<Boolean> onStructuredOutputChanged,
            Consumer<Boolean> onSetDefaultChanged,
            Runnable onCancel,
            Runnable onSave,
            Style style,
            CheckboxBlock.Style checkboxStyle
    ) {
        UiRect rect = ConfigUiModalSupport.modelSettingsModalRect(screenWidth, screenHeight);
        int rowY = rect.y + 48;
        int labelWidth = 130;
        int fieldX = rect.x + 24 + labelWidth + 8;
        int fieldWidth = rect.width - 24 - 24 - labelWidth - 8;

        floatingActionBlockAdder.add(
                rect.x + 24,
                rowY,
                labelWidth,
                20,
                () -> translator.t("modal.model.name"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );

        TextFieldWidget modelNameField = floatingTextFieldAdder.add(
                fieldX,
                rowY,
                fieldWidth,
                128,
                modelSettingsDraft,
                translator.t("placeholder.model_id"),
                onModelIdChanged,
                true
        );
        rowY += 24;

        floatingActionBlockAdder.add(
                rect.x + 24,
                rowY,
                labelWidth,
                20,
                () -> translator.t("modal.model.temperature"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );
        floatingTextFieldAdder.add(
                fieldX,
                rowY,
                fieldWidth,
                24,
                modelSettingsTemperatureDraft,
                translator.t("modal.model.temperature"),
                onTemperatureChanged,
                true
        );
        rowY += 24;

        if (profile.type == ApiProviderType.OLLAMA) {
            floatingActionBlockAdder.add(
                    rect.x + 24,
                    rowY,
                    labelWidth,
                    20,
                    () -> translator.t("modal.model.keep_alive"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorText(),
                    false
            );
            floatingTextFieldAdder.add(
                    fieldX,
                    rowY,
                    fieldWidth,
                    64,
                    modelSettingsKeepAliveDraft,
                    translator.t("modal.model.keep_alive"),
                    onKeepAliveChanged,
                    true
            );
            rowY += 24;
        }

        floatingActionBlockAdder.add(
                rect.x + 24,
                rowY,
                labelWidth,
                20,
                () -> translator.t("modal.model.system_prompt_suffix"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );
        floatingTextFieldAdder.add(
                fieldX,
                rowY,
                fieldWidth,
                256,
                modelSettingsSystemPromptSuffixDraft,
                translator.t("modal.model.system_prompt_suffix"),
                onSystemPromptSuffixChanged,
                true
        );
        rowY += 24;

        floatingActionBlockAdder.add(
                rect.x + 24,
                rowY,
                labelWidth,
                20,
                () -> translator.t("modal.model.custom_parameters"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );
        floatingActionBlockAdder.add(
                fieldX,
                rowY,
                fieldWidth,
                20,
                () -> translator.t("button.edit_custom_parameters", customParameterCount),
                onEditCustomParameters,
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                false
        );
        rowY += 24;

        floatingCheckboxAdder.add(
                rect.x + 24,
                rowY,
                rect.width - 48,
                20,
                () -> translator.t("modal.model.supports_system_msg"),
                () -> modelSettingsSupportsSystemDraft,
                onSupportsSystemChanged,
                checkboxStyle
        );
        rowY += 24;

        if (!modelSettingsSupportsSystemDraft) {
            floatingCheckboxAdder.add(
                    rect.x + 24,
                    rowY,
                    rect.width - 48,
                    20,
                    () -> translator.t("modal.model.inject_prompt_into_user"),
                    () -> modelSettingsInjectPromptIntoUserDraft,
                    onInjectPromptIntoUserChanged,
                    checkboxStyle
            );
            rowY += 24;
        }

        floatingCheckboxAdder.add(
                rect.x + 24,
                rowY,
                rect.width - 48,
                20,
                () -> translator.t("modal.model.structured_output"),
                () -> modelSettingsStructuredOutputDraft,
                onStructuredOutputChanged,
                checkboxStyle
        );
        rowY += 24;

        floatingCheckboxAdder.add(
                rect.x + 24,
                rowY,
                rect.width - 48,
                20,
                () -> translator.t("modal.model.set_default"),
                () -> modelSettingsSetDefault,
                onSetDefaultChanged,
                checkboxStyle
        );

        int buttonsY = rect.y + rect.height - 32;
        int half = (rect.width - 24 - 24 - 6) / 2;
        int leftX = rect.x + 24;
        int rightX = leftX + half + 6;

        floatingActionBlockAdder.add(
                leftX,
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
                rightX,
                buttonsY,
                half,
                20,
                () -> translator.t("button.model_save"),
                onSave,
                style.colorBlockAccent(),
                style.colorBlockAccentHover(),
                style.colorText(),
                true
        );

        return modelNameField;
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
    public interface FloatingCheckboxAdder {
        void add(
                int x,
                int y,
                int width,
                int height,
                Supplier<Text> labelSupplier,
                BooleanSupplier checked,
                Consumer<Boolean> changed,
                CheckboxBlock.Style style
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
            int colorBlock,
            int colorBlockHover,
            int colorBlockSelected,
            int colorBlockSelectedHover,
            int colorBlockAccent,
            int colorBlockAccentHover,
            int colorText
    ) {
    }
}
