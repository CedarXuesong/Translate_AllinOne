package com.cedarxuesong.translate_allinone.utils.config.pojos;

public class InputBindingConfig {
    public InputType type = InputType.KEYSYM;
    public int code = -1;

    public enum InputType {
        KEYSYM,
        MOUSE
    }

    public boolean isBound() {
        return code >= 0;
    }
}
