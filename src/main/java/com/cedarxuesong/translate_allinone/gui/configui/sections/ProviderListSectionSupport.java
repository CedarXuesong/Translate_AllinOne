package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderProfileSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ProviderListSectionSupport {
    private static final int ROW_STEP = 24;

    private ProviderListSectionSupport() {
    }

    public static RenderResult render(
            ProviderManagerConfig providerManager,
            int x,
            int y,
            int width,
            String providerSearchQuery,
            String selectedProviderId,
            Translator translator,
            ActionBlockAdder actionBlockAdder,
            TextFieldAdder textFieldAdder,
            Consumer<String> onSearchChanged,
            Consumer<String> onProviderSelected,
            Runnable onOpenAddProvider,
            Style style
    ) {
        int searchY = y;
        TextFieldWidget providerSearchField = textFieldAdder.add(
                x,
                searchY,
                width,
                64,
                providerSearchQuery,
                translator.t("placeholder.search_platform"),
                onSearchChanged,
                true
        );

        int listStartY = searchY + 24;
        int rowY = listStartY;

        List<ApiProviderProfile> filtered = ProviderProfileSupport.filterProviders(providerManager.providers, providerSearchQuery);
        if (providerManager.providers == null || providerManager.providers.isEmpty()) {
            actionBlockAdder.add(
                    x,
                    listStartY,
                    width,
                    44,
                    () -> translator.t("empty.providers_hint"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
            rowY += 44 + 4;
        } else if (filtered.isEmpty()) {
            actionBlockAdder.add(
                    x,
                    listStartY,
                    width,
                    44,
                    () -> translator.t("empty.search_no_match"),
                    () -> {
                    },
                    style.colorBlockMuted(),
                    style.colorBlockMuted(),
                    style.colorTextMuted(),
                    false
            );
            rowY += 44 + 4;
        } else {
            for (ApiProviderProfile profile : filtered) {
                boolean selected = profile != null && profile.id != null && profile.id.equals(selectedProviderId);
                String statusText = profile != null && profile.enabled
                        ? translator.t("state.on").getString()
                        : translator.t("state.off").getString();
                String providerName = ProviderProfileSupport.safeProviderName(profile);
                String profileId = profile == null ? "" : profile.id;

                actionBlockAdder.add(
                        x,
                        rowY,
                        width,
                        20,
                        () -> Text.literal("[P] " + providerName + "  " + statusText),
                        () -> onProviderSelected.accept(profileId),
                        selected ? style.colorBlockSelected() : style.colorBlock(),
                        selected ? style.colorBlockSelectedHover() : style.colorBlockHover(),
                        selected ? style.colorTextAccent() : style.colorText(),
                        false
                );

                rowY += ROW_STEP;
            }
        }

        int addButtonY = rowY;

        actionBlockAdder.add(
                x,
                addButtonY,
                width,
                20,
                () -> translator.t("button.add_provider"),
                onOpenAddProvider,
                style.colorBlockAccent(),
                style.colorBlockAccentHover(),
                style.colorText(),
                true
        );

        return new RenderResult(providerSearchField, addButtonY + 20);
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
    public interface Translator {
        Text t(String key, Object... args);
    }

    public record RenderResult(TextFieldWidget providerSearchField, int contentBottomY) {
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
            int colorBlockAccentHover
    ) {
    }
}
