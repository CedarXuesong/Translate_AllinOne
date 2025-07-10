package com.cedarxuesong.translate_allinone.mixin.mixinInGameGui;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Comparator;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
    @Accessor("SCOREBOARD_ENTRY_COMPARATOR")
    static Comparator<ScoreboardEntry> getScoreboardEntryComparator() {
        throw new AssertionError();
    }
} 