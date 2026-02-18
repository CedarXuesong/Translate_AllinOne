package com.cedarxuesong.translate_allinone.utils.config.pojos;

public class ItemTranslateConfig {
    public boolean enabled = false;
    public boolean enabled_translate_item_custom_name = false;
    public boolean enabled_translate_item_lore = false;
    public int max_concurrent_requests = 2;
    public int requests_per_minute = 60;
    public int max_batch_size = 10;
    public String target_language = "Chinese";
    public KeybindingConfig keybinding = new KeybindingConfig();

    public enum KeybindingMode {
        HOLD_TO_TRANSLATE,
        HOLD_TO_SEE_ORIGINAL,
        DISABLED
    }

    public static class KeybindingConfig {
        public KeybindingMode mode = KeybindingMode.DISABLED;
        public InputBindingConfig binding = new InputBindingConfig();
    }
}
