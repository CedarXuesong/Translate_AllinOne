package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderEditorSupport;
import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderProfileSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderType;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ProviderDetailSectionSupport {
    private static final int ROW_STEP = 24;
    private static final int MIN_CONTENT_HEIGHT = 24;
    private static final int GROUP_GAP = 34;
    private static final int GROUP_PADDING_TOP = 18;
    private static final int GROUP_PADDING_BOTTOM = 8;
    private static final int GROUP_PADDING_SIDE = 6;

    private ProviderDetailSectionSupport() {
    }

    public static int render(
            ApiProviderProfile profile,
            int x,
            int y,
            int width,
            int height,
            boolean providerApiKeyVisible,
            Translator translator,
            GroupBoxAdder groupBoxAdder,
            ProviderTypeLabelProvider providerTypeLabelProvider,
            ActionBlockAdder actionBlockAdder,
            TextFieldAdder textFieldAdder,
            Consumer<ApiProviderProfile> onToggleProviderEnabled,
            Consumer<ApiProviderProfile> onDeleteProvider,
            Runnable onToggleApiKeyVisible,
            Consumer<ApiProviderProfile> onTestProviderConnection,
            BiConsumer<ApiProviderProfile, String> onSetDefaultModel,
            BiConsumer<ApiProviderProfile, String> onOpenModelSettings,
            BiConsumer<ApiProviderProfile, String> onRemoveModel,
            Consumer<ApiProviderProfile> onAddModel,
            Style style
    ) {
        if (profile == null) {
            actionBlockAdder.add(
                    x,
                    y,
                    width,
                    20,
                    () -> translator.t("empty.provider_detail"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
            addGroupBox(groupBoxAdder, translator.t("group.providers.info"), x, width, y, y + ROW_STEP);
            return y + ROW_STEP;
        }

        profile.ensureModelSettings();

        int rowY = y;
        int infoStartY = rowY;
        int toggleWidth = 82;
        int deleteWidth = 92;
        int toggleX = x;
        int deleteProviderX = toggleX + toggleWidth + 4;

        actionBlockAdder.add(
                toggleX,
                rowY,
                toggleWidth,
                20,
                () -> translator.t("label.toggle", translator.t("label.enabled"), profile.enabled ? translator.t("state.on") : translator.t("state.off")),
                () -> onToggleProviderEnabled.accept(profile),
                profile.enabled ? style.colorBlockSelected() : style.colorBlock(),
                profile.enabled ? style.colorBlockSelectedHover() : style.colorBlockHover(),
                style.colorText(),
                true
        );

        actionBlockAdder.add(
                deleteProviderX,
                rowY,
                deleteWidth,
                20,
                () -> translator.t("button.delete_provider"),
                () -> onDeleteProvider.accept(profile),
                style.colorBlockDanger(),
                style.colorBlockDangerHover(),
                style.colorText(),
                true
        );
        rowY += ROW_STEP;

        rowY = addProviderTextFieldRow(
                x,
                rowY,
                width,
                translator.t("label.profile_name"),
                profile.name,
                value -> profile.name = ProviderProfileSupport.sanitizeText(value),
                80,
                true,
                actionBlockAdder,
                textFieldAdder,
                style
        );

        actionBlockAdder.add(
                x,
                rowY,
                width,
                20,
                () -> translator.t("label.provider_id_value", profile.id),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorTextMuted(),
                false
        );
        rowY += ROW_STEP;

        int labelWidth = 120;
        int fieldX = x + labelWidth + 6;
        int fieldWidth = Math.max(40, width - labelWidth - 6);
        actionBlockAdder.add(
                x,
                rowY,
                labelWidth,
                20,
                () -> translator.t("label.provider_type"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );
        actionBlockAdder.add(
                fieldX,
                rowY,
                fieldWidth,
                20,
                () -> providerTypeLabelProvider.label(profile.type),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );
        rowY += ROW_STEP;

        rowY = addApiKeyRow(
                profile,
                x,
                rowY,
                width,
                providerApiKeyVisible,
                translator,
                actionBlockAdder,
                textFieldAdder,
                onToggleApiKeyVisible,
                onTestProviderConnection,
                style
        );

        rowY = addProviderTextFieldRow(
                x,
                rowY,
                width,
                translator.t("label.base_url"),
                profile.base_url,
                value -> profile.base_url = ProviderProfileSupport.sanitizeText(value),
                256,
                true,
                actionBlockAdder,
                textFieldAdder,
                style
        );

        actionBlockAdder.add(
                x,
                rowY,
                width,
                20,
                () -> translator.t("label.request_preview", ProviderEditorSupport.previewRequestAddress(profile)),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorTextMuted(),
                false
        );
        rowY += ROW_STEP;

        int infoEndY = rowY;
        addGroupBox(groupBoxAdder, translator.t("group.providers.info"), x, width, infoStartY, infoEndY);

        rowY += GROUP_GAP;
        int modelsStartY = rowY;

        List<String> modelIds = ProviderProfileSupport.normalizeModelIds(profile);
        if (modelIds.isEmpty()) {
            actionBlockAdder.add(
                    x,
                    rowY,
                    width,
                    20,
                    () -> translator.t("label.no_models"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
            rowY += ROW_STEP;
        } else {
            for (String modelId : modelIds) {
                boolean active = modelId.equals(profile.model_id);
                int settingsWidth = 26;
                int deleteModelWidth = 26;
                int mainWidth = Math.max(60, width - settingsWidth - deleteModelWidth - 8);
                int settingsX = x + mainWidth + 4;
                int deleteX = settingsX + settingsWidth + 4;

                actionBlockAdder.add(
                        x,
                        rowY,
                        mainWidth,
                        20,
                        () -> Text.literal("[M] " + modelId + (active ? "  *" : "")),
                        () -> onSetDefaultModel.accept(profile, modelId),
                        active ? style.colorBlockSelected() : style.colorBlock(),
                        active ? style.colorBlockSelectedHover() : style.colorBlockHover(),
                        active ? style.colorTextAccent() : style.colorText(),
                        false
                );

                actionBlockAdder.add(
                        settingsX,
                        rowY,
                        settingsWidth,
                        20,
                        () -> Text.literal("S"),
                        () -> onOpenModelSettings.accept(profile, modelId),
                        style.colorBlock(),
                        style.colorBlockHover(),
                        style.colorText(),
                        true
                );

                actionBlockAdder.add(
                        deleteX,
                        rowY,
                        deleteModelWidth,
                        20,
                        () -> Text.literal("X"),
                        () -> onRemoveModel.accept(profile, modelId),
                        style.colorBlockDanger(),
                        style.colorBlockDangerHover(),
                        style.colorText(),
                        true
                );

                rowY += ROW_STEP;
            }
        }

        actionBlockAdder.add(
                x,
                rowY,
                width,
                20,
                () -> translator.t("button.add_model"),
                () -> onAddModel.accept(profile),
                style.colorBlockAccent(),
                style.colorBlockAccentHover(),
                style.colorText(),
                true
        );

        addGroupBox(groupBoxAdder, translator.t("group.providers.models"), x, width, modelsStartY, rowY + ROW_STEP);
        return rowY + ROW_STEP;
    }

    private static void addGroupBox(
            GroupBoxAdder groupBoxAdder,
            Text title,
            int x,
            int width,
            int contentStartY,
            int contentEndY
    ) {
        if (groupBoxAdder == null) {
            return;
        }

        int contentBottom = Math.max(contentStartY + MIN_CONTENT_HEIGHT, contentEndY);
        int groupX = x - GROUP_PADDING_SIDE;
        int groupY = contentStartY - GROUP_PADDING_TOP;
        int groupWidth = width + GROUP_PADDING_SIDE * 2;
        int groupHeight = (contentBottom - contentStartY) + GROUP_PADDING_TOP + GROUP_PADDING_BOTTOM;
        groupBoxAdder.add(groupX, groupY, groupWidth, groupHeight, title);
    }

    private static int addApiKeyRow(
            ApiProviderProfile profile,
            int x,
            int y,
            int width,
            boolean providerApiKeyVisible,
            Translator translator,
            ActionBlockAdder actionBlockAdder,
            TextFieldAdder textFieldAdder,
            Runnable onToggleApiKeyVisible,
            Consumer<ApiProviderProfile> onTestProviderConnection,
            Style style
    ) {
        int labelWidth = 120;
        int operationWidth = 56;
        int fieldX = x + labelWidth + 6;
        int fieldWidth = Math.max(40, width - labelWidth - 6 - operationWidth - operationWidth - 8);
        int toggleX = fieldX + fieldWidth + 4;
        int testX = toggleX + operationWidth + 4;

        actionBlockAdder.add(
                x,
                y,
                labelWidth,
                20,
                () -> translator.t("label.api_key"),
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );

        String rawApiKey = ProviderProfileSupport.sanitizeText(profile.api_key);
        String fieldValue = providerApiKeyVisible ? rawApiKey : ProviderEditorSupport.maskApiKey(rawApiKey);
        textFieldAdder.add(
                fieldX,
                y,
                fieldWidth,
                256,
                fieldValue,
                translator.t("label.api_key"),
                value -> {
                    if (providerApiKeyVisible) {
                        profile.api_key = ProviderProfileSupport.sanitizeText(value);
                    }
                },
                providerApiKeyVisible
        );

        actionBlockAdder.add(
                toggleX,
                y,
                operationWidth,
                20,
                () -> providerApiKeyVisible ? translator.t("button.hide_key") : translator.t("button.show_key"),
                onToggleApiKeyVisible,
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                true
        );

        actionBlockAdder.add(
                testX,
                y,
                operationWidth,
                20,
                () -> translator.t("button.test"),
                () -> onTestProviderConnection.accept(profile),
                style.colorBlock(),
                style.colorBlockHover(),
                style.colorText(),
                true
        );

        return y + 24;
    }

    private static int addProviderTextFieldRow(
            int x,
            int y,
            int width,
            Text label,
            String initialValue,
            Consumer<String> onChanged,
            int maxLength,
            boolean editable,
            ActionBlockAdder actionBlockAdder,
            TextFieldAdder textFieldAdder,
            Style style
    ) {
        int labelWidth = Math.min(180, Math.max(120, width / 3));
        int fieldX = x + labelWidth + 6;
        int fieldWidth = Math.max(40, width - labelWidth - 6);

        actionBlockAdder.add(
                x,
                y,
                labelWidth,
                20,
                () -> label,
                () -> {
                },
                style.colorBlockMuted(),
                style.colorBlockMuted(),
                style.colorText(),
                false
        );
        textFieldAdder.add(fieldX, y, fieldWidth, maxLength, initialValue, label, onChanged, editable);
        return y + 24;
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
    public interface TextFieldAdder {
        void add(
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
    public interface Translator {
        Text t(String key, Object... args);
    }

    @FunctionalInterface
    public interface GroupBoxAdder {
        void add(int x, int y, int width, int height, Text title);
    }

    @FunctionalInterface
    public interface ProviderTypeLabelProvider {
        Text label(ApiProviderType providerType);
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
            int colorBlockDanger,
            int colorBlockDangerHover,
            int colorBlockAccent,
            int colorBlockAccentHover
    ) {
    }
}
