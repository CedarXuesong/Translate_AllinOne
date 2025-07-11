package com.cedarxuesong.translate_allinone.utils.config;

import com.cedarxuesong.translate_allinone.Translate_AllinOne;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ChatTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ItemTranslateConfig;
import com.cedarxuesong.translate_allinone.utils.config.pojos.ScoreboardConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Translate_AllinOne.MOD_ID + "/" + Translate_AllinOne.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/amethyst_block.png")
public class ModConfig implements ConfigData {

    @ConfigEntry.Category("chat_translate")
    @ConfigEntry.Gui.TransitiveObject
    public ChatTranslateConfig chatTranslate = new ChatTranslateConfig();

    @ConfigEntry.Category("item_translate")
    @ConfigEntry.Gui.TransitiveObject
    public ItemTranslateConfig itemTranslate = new ItemTranslateConfig();

    @ConfigEntry.Category("scoreboard_translate")
    @ConfigEntry.Gui.TransitiveObject
    public ScoreboardConfig scoreboardTranslate = new ScoreboardConfig();

}
