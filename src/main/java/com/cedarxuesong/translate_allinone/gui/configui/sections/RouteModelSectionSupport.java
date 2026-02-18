package com.cedarxuesong.translate_allinone.gui.configui.sections;

import com.cedarxuesong.translate_allinone.gui.configui.model.RouteModelOption;
import com.cedarxuesong.translate_allinone.gui.configui.model.RouteSlot;
import com.cedarxuesong.translate_allinone.gui.configui.support.ProviderProfileSupport;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ProviderManagerConfig;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class RouteModelSectionSupport {
    private RouteModelSectionSupport() {
    }

    public static List<RouteModelOption> buildRouteModelOptions(ProviderManagerConfig manager, Text noneOptionLabel) {
        List<RouteModelOption> options = new ArrayList<>();
        options.add(new RouteModelOption("", noneOptionLabel));

        if (manager.providers == null) {
            return options;
        }

        for (ApiProviderProfile profile : manager.providers) {
            if (profile == null || profile.id == null || profile.id.isBlank()) {
                continue;
            }
            profile.ensureModelSettings();
            for (String modelId : ProviderProfileSupport.normalizeModelIds(profile)) {
                if (modelId == null || modelId.isBlank()) {
                    continue;
                }
                String routeKey = ProviderManagerConfig.composeRouteKey(profile.id, modelId);
                Text display = Text.literal(ProviderProfileSupport.safeProviderName(profile) + "/" + modelId);
                options.add(new RouteModelOption(routeKey, display));
            }
        }
        return options;
    }

    public static String getRouteKey(ProviderManagerConfig manager, RouteSlot routeSlot) {
        return switch (routeSlot) {
            case ITEM -> manager.routes.item;
            case SCOREBOARD -> manager.routes.scoreboard;
            case CHAT_INPUT -> manager.routes.chat_input;
            case CHAT_OUTPUT -> manager.routes.chat_output;
        };
    }

    public static void setRouteKey(ProviderManagerConfig manager, RouteSlot routeSlot, String routeKey) {
        switch (routeSlot) {
            case ITEM -> manager.routes.item = routeKey;
            case SCOREBOARD -> manager.routes.scoreboard = routeKey;
            case CHAT_INPUT -> manager.routes.chat_input = routeKey;
            case CHAT_OUTPUT -> manager.routes.chat_output = routeKey;
        }
    }

    public static Text describeRouteModel(
            String routeKey,
            ProviderManagerConfig manager,
            Text noneLabel,
            Function<String, Text> missingLabelFactory
    ) {
        if (routeKey == null || routeKey.isBlank()) {
            return noneLabel;
        }

        String providerId = ProviderManagerConfig.extractProviderId(routeKey);
        String modelId = ProviderManagerConfig.extractModelId(routeKey);
        if (providerId.isBlank() || modelId.isBlank()) {
            return missingLabelFactory.apply(routeKey);
        }

        ApiProviderProfile profile = manager.findById(providerId);
        if (profile == null) {
            return missingLabelFactory.apply(routeKey);
        }
        profile.ensureModelSettings();
        if (profile.getModelSettings(modelId) == null) {
            return missingLabelFactory.apply(routeKey);
        }
        return Text.literal(ProviderProfileSupport.safeProviderName(profile) + "/" + modelId);
    }

    public static void clearRouteIfMatched(ProviderManagerConfig manager, String providerId) {
        if (manager.routes == null || providerId == null || providerId.isBlank()) {
            return;
        }

        if (ProviderManagerConfig.extractProviderId(manager.routes.item).equals(providerId)) {
            manager.routes.item = "";
        }
        if (ProviderManagerConfig.extractProviderId(manager.routes.scoreboard).equals(providerId)) {
            manager.routes.scoreboard = "";
        }
        if (ProviderManagerConfig.extractProviderId(manager.routes.chat_input).equals(providerId)) {
            manager.routes.chat_input = "";
        }
        if (ProviderManagerConfig.extractProviderId(manager.routes.chat_output).equals(providerId)) {
            manager.routes.chat_output = "";
        }
    }
}
