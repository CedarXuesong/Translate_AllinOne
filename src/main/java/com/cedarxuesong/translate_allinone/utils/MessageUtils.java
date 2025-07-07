package com.cedarxuesong.translate_allinone.utils;

import net.minecraft.text.Text;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessageUtils {
    public static final Map<UUID, Text> MESSAGES_BY_UUID = new ConcurrentHashMap<>();
} 