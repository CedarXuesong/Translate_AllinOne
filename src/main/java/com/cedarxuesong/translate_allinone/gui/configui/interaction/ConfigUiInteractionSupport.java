package com.cedarxuesong.translate_allinone.gui.configui.interaction;

import com.cedarxuesong.translate_allinone.gui.configui.controls.ActionBlock;
import com.cedarxuesong.translate_allinone.gui.configui.controls.CheckboxBlock;
import com.cedarxuesong.translate_allinone.gui.configui.controls.IntSliderBlock;

import java.util.List;

public final class ConfigUiInteractionSupport {
    private ConfigUiInteractionSupport() {
    }

    public static boolean dispatchActionBlocks(List<ActionBlock> blocks, double mouseX, double mouseY) {
        for (int i = blocks.size() - 1; i >= 0; i--) {
            ActionBlock block = blocks.get(i);
            if (block.contains(mouseX, mouseY)) {
                block.runAction();
                return true;
            }
        }
        return false;
    }

    public static boolean dispatchCheckboxBlocks(List<CheckboxBlock> blocks, double mouseX, double mouseY) {
        for (int i = blocks.size() - 1; i >= 0; i--) {
            CheckboxBlock block = blocks.get(i);
            if (block.contains(mouseX, mouseY)) {
                block.toggle();
                return true;
            }
        }
        return false;
    }

    public static IntSliderBlock pickSlider(List<IntSliderBlock> sliders, double mouseX, double mouseY) {
        for (IntSliderBlock slider : sliders) {
            if (slider.contains(mouseX, mouseY)) {
                return slider;
            }
        }
        return null;
    }
}
