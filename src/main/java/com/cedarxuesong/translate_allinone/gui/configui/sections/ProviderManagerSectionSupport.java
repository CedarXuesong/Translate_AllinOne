package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderProfileSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ProviderManagerSectionSupport {
    private static final int CONTENT_TOP_INSET = 16;
    private static final int COLUMN_GAP = 16;
    private static final int MIN_CONTENT_HEIGHT = 24;
    private static final int GROUP_PADDING_TOP = 18;
    private static final int GROUP_PADDING_BOTTOM = 8;
    private static final int GROUP_PADDING_SIDE = 6;

    private ProviderManagerSectionSupport() {
    }

    public static RenderResult render(
            ProviderManagerConfig providerManager,
            int viewportHeight,
            int x,
            int y,
            int width,
            String providerSearchQuery,
            String selectedProviderId,
            int selectedProviderIndex,
            boolean providerApiKeyVisible,
            ProviderListSectionSupport.Style providerListStyle,
            ProviderDetailSectionSupport.Style providerDetailStyle,
            Translator translator,
            ProviderDetailSectionSupport.GroupBoxAdder groupBoxAdder,
            ProviderDetailSectionSupport.ProviderTypeLabelProvider providerTypeLabelProvider,
            ProviderListSectionSupport.ActionBlockAdder listActionBlockAdder,
            ProviderListSectionSupport.TextFieldAdder listTextFieldAdder,
            Consumer<String> onSearchChanged,
            Consumer<String> onProviderSelected,
            Runnable onOpenAddProvider,
            ProviderDetailSectionSupport.ActionBlockAdder detailActionBlockAdder,
            ProviderDetailSectionSupport.TextFieldAdder detailTextFieldAdder,
            Consumer<ApiProviderProfile> onToggleProviderEnabled,
            Consumer<ApiProviderProfile> onDeleteProvider,
            Runnable onToggleApiKeyVisible,
            Consumer<ApiProviderProfile> onTestProviderConnection,
            BiConsumer<ApiProviderProfile, String> onSetDefaultModel,
            BiConsumer<ApiProviderProfile, String> onOpenModelSettings,
            BiConsumer<ApiProviderProfile, String> onRemoveModel,
            Consumer<ApiProviderProfile> onAddModel
    ) {
        int workspaceHeight = Math.max(120, viewportHeight - 12);
        int contentY = y + CONTENT_TOP_INSET;
        int contentHeight = Math.max(120, workspaceHeight - CONTENT_TOP_INSET);
        int listWidth = Math.max(220, Math.min(300, width / 3));
        int detailX = x + listWidth + COLUMN_GAP;
        int detailWidth = Math.max(260, width - listWidth - COLUMN_GAP);

        ProviderListSectionSupport.RenderResult listResult = ProviderListSectionSupport.render(
                providerManager,
                x,
                contentY,
                listWidth,
                providerSearchQuery,
                selectedProviderId,
                translator::t,
                listActionBlockAdder,
                listTextFieldAdder,
                onSearchChanged,
                onProviderSelected,
                onOpenAddProvider,
                providerListStyle
        );

        int leftContentBottom = Math.max(contentY + contentHeight, listResult.contentBottomY());

        addGroupBox(
                groupBoxAdder,
                translator.t("group.providers.list"),
                x,
                listWidth,
                contentY,
                leftContentBottom
        );

        ProviderProfileSupport.SelectedProvider selected = ProviderProfileSupport.resolveSelectedProvider(
                providerManager,
                selectedProviderId,
                selectedProviderIndex
        );

        int detailContentBottom = ProviderDetailSectionSupport.render(
                selected.profile(),
                detailX,
                contentY,
                detailWidth,
                contentHeight,
                providerApiKeyVisible,
                translator::t,
                groupBoxAdder,
                providerTypeLabelProvider,
                detailActionBlockAdder,
                detailTextFieldAdder,
                onToggleProviderEnabled,
                onDeleteProvider,
                onToggleApiKeyVisible,
                onTestProviderConnection,
                onSetDefaultModel,
                onOpenModelSettings,
                onRemoveModel,
                onAddModel,
                providerDetailStyle
        );

        int totalContentBottom = Math.max(leftContentBottom, Math.max(contentY + contentHeight, detailContentBottom));
        return new RenderResult(listResult.providerSearchField(), selected.selectedProviderId(), totalContentBottom);
    }

    private static void addGroupBox(
            ProviderDetailSectionSupport.GroupBoxAdder groupBoxAdder,
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

    @FunctionalInterface
    public interface Translator {
        Text t(String key, Object... args);
    }

    public record RenderResult(TextFieldWidget providerSearchField, String selectedProviderId, int contentBottomY) {
    }
}
