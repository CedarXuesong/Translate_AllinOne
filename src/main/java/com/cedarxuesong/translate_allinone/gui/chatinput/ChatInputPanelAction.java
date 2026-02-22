package com.cedarxuesong.translate_allinone.gui.chatinput;

import net.minecraft.text.Text;

public enum ChatInputPanelAction {
    TRANSLATE("text.translate_allinone.chat_input_panel.translate", "T", 0xFF569BE6),
    PROFESSIONAL("text.translate_allinone.chat_input_panel.professional", "P", 0xFF4CB08A),
    FRIENDLY("text.translate_allinone.chat_input_panel.friendly", "F", 0xFFE0A75B),
    EXPAND("text.translate_allinone.chat_input_panel.expand", "+", 0xFF79AEDC),
    CONCISE("text.translate_allinone.chat_input_panel.concise", "-", 0xFF7FC188),
    RESTORE("text.translate_allinone.chat_input_panel.restore", "R", 0xFFC77A7A);

    private final String key;
    private final String icon;
    private final int accentColor;

    ChatInputPanelAction(String key, String icon, int accentColor) {
        this.key = key;
        this.icon = icon;
        this.accentColor = accentColor;
    }

    public Text label() {
        return Text.translatable(this.key);
    }

    public String icon() {
        return this.icon;
    }

    public int accentColor() {
        return this.accentColor;
    }
}
