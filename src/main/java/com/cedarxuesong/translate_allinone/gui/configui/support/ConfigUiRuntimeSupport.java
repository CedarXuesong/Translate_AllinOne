package com.cedarxuesong.translate_allinone.gui.configui.support;

import com.cedarxuesong.translate_allinone.registration.ConfigManager;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ApiProviderProfile;
import com.cedarxuesong.translate_allinone.utils.llmapi.ProviderConnectionTester;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class ConfigUiRuntimeSupport {
    private ConfigUiRuntimeSupport() {
    }

    public static boolean saveConfig(
            Translator translator,
            StatusSetter statusSetter,
            int okColor,
            int errorColor,
            ErrorLogger errorLogger
    ) {
        try {
            ConfigManager.save();
            statusSetter.set(translator.t("status.config_saved", ConfigManager.getConfigPath().getFileName()), okColor);
            return true;
        } catch (Exception e) {
            errorLogger.error("Failed to save config", e);
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            statusSetter.set(translator.t("error.save_failed", reason), errorColor);
            return false;
        }
    }

    public static void testProviderConnection(
            ApiProviderProfile profile,
            Translator translator,
            StatusSetter statusSetter,
            int okColor,
            int errorColor,
            Consumer<Runnable> uiThreadExecutor
    ) {
        statusSetter.set(translator.t("status.testing_provider", profile.name), okColor);
        ProviderConnectionTester.test(profile).whenComplete((result, throwable) -> {
            Runnable updateStatus = () -> {
                if (throwable != null) {
                    String reason = throwable.getMessage() == null ? "request failed" : throwable.getMessage();
                    statusSetter.set(translator.t("status.test_failed", profile.name, reason), errorColor);
                    return;
                }

                if (result == null) {
                    statusSetter.set(translator.t("status.test_failed", profile.name, "null result"), errorColor);
                    return;
                }

                if (result.success()) {
                    statusSetter.set(translator.t("status.test_success", profile.name, result.detail()), okColor);
                } else {
                    statusSetter.set(translator.t("status.test_failed", profile.name, result.detail()), errorColor);
                }
            };

            uiThreadExecutor.accept(updateStatus);
        });
    }

    @FunctionalInterface
    public interface Translator {
        Text t(String key, Object... args);
    }

    @FunctionalInterface
    public interface StatusSetter {
        void set(Text message, int color);
    }

    @FunctionalInterface
    public interface ErrorLogger {
        void error(String message, Throwable throwable);
    }
}
