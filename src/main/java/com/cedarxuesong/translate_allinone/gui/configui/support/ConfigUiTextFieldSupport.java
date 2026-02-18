package com.cedarxuesong.translate_allinone.gui.configui.support;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public final class ConfigUiTextFieldSupport {
    private ConfigUiTextFieldSupport() {
    }

    public static TextFieldWidget create(
            TextRenderer textRenderer,
            Consumer<TextFieldWidget> registerField,
            List<TextFieldWidget> providerEditorFields,
            List<TextFieldWidget> floatingEditorFields,
            int x,
            int y,
            int width,
            int maxLength,
            String initialValue,
            Text placeholder,
            Consumer<String> changed,
            boolean editable,
            boolean floating,
            boolean modalOpen
    ) {
        int renderX = x;
        int renderY = y;
        if (modalOpen && !floating) {
            renderX = -10000;
            renderY = -10000;
        }

        TextFieldWidget field = new TextFieldWidget(textRenderer, renderX, renderY, width, 20, Text.empty());
        field.setMaxLength(maxLength);
        field.setText(initialValue == null ? "" : initialValue);
        field.setChangedListener(changed);
        field.setPlaceholder(placeholder);
        field.setEditable(editable);

        registerField.accept(field);
        if (floating) {
            floatingEditorFields.add(field);
        } else {
            providerEditorFields.add(field);
        }

        return field;
    }
}
