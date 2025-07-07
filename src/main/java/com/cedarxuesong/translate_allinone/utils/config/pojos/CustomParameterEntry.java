package com.cedarxuesong.translate_allinone.utils.config.pojos;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class CustomParameterEntry {
    @ConfigEntry.Gui.RequiresRestart(value = false)
    public String key = "parameter_name";
    @ConfigEntry.Gui.RequiresRestart(value = false)
    public String value = "parameter_value";
} 