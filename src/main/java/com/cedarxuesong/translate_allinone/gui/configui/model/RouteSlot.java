package com.cedarxuesong.translate_allinone.gui.configui.model;

public enum RouteSlot {
    ITEM("item"),
    SCOREBOARD("scoreboard"),
    CHAT_INPUT("chat_input"),
    CHAT_OUTPUT("chat_output");

    private final String key;

    RouteSlot(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String translationKey() {
        return "route." + key;
    }
}
