package com.cedarxuesong.translate_allinone.mixin.mixinChatHud;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {
    @Accessor("messages")
    List<ChatHudLine> getMessages();

    @Accessor("scrolledLines")
    int getScrolledLines();

    @Accessor("scrolledLines")
    void setScrolledLines(int scrolledLines);

    @Invoker("refresh")
    void invokeRefresh();
} 