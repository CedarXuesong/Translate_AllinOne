package com.cedarxuesong.translate_allinone.utils;

import net.minecraft.text.Text;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageUtils {
    private static final int MAX_TRACKED_MESSAGES = 1024;
    private static final Map<UUID, Text> MESSAGES_BY_UUID = new ConcurrentHashMap<>();
    private static final Queue<UUID> INSERTION_ORDER = new ConcurrentLinkedQueue<>();

    private MessageUtils() {
    }

    public static void putTrackedMessage(UUID messageId, Text message) {
        if (messageId == null || message == null) {
            return;
        }

        boolean isNewMessage = MESSAGES_BY_UUID.put(messageId, message) == null;
        if (isNewMessage) {
            INSERTION_ORDER.offer(messageId);
        }
        trimIfNeeded();
    }

    public static Text getTrackedMessage(UUID messageId) {
        if (messageId == null) {
            return null;
        }
        return MESSAGES_BY_UUID.get(messageId);
    }

    public static void removeTrackedMessage(UUID messageId) {
        if (messageId == null) {
            return;
        }
        if (MESSAGES_BY_UUID.remove(messageId) != null) {
            INSERTION_ORDER.remove(messageId);
        }
    }

    private static void trimIfNeeded() {
        while (MESSAGES_BY_UUID.size() > MAX_TRACKED_MESSAGES) {
            UUID oldestId = INSERTION_ORDER.poll();
            if (oldestId == null) {
                break;
            }
            MESSAGES_BY_UUID.remove(oldestId);
        }
    }
}
