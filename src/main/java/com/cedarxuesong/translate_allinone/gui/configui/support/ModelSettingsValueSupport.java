package com.cedarxuesong.translate_allinone.gui.configui.support;

public final class ModelSettingsValueSupport {
    private ModelSettingsValueSupport() {
    }

    public static String formatTemperature(double temperature) {
        return Double.toString(temperature);
    }

    public static Double parseTemperatureInput(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static String normalizeKeepAliveInput(String raw) {
        String value = ProviderProfileSupport.sanitizeText(raw).trim();
        return value.isEmpty() ? "1m" : value;
    }
}
