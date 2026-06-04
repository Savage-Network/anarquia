package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.LobbySettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatSpamTracker {

    private static final Map<UUID, List<Entry>> RECENT = new ConcurrentHashMap<>();

    private ChatSpamTracker() {}

    public static boolean isSpam(UUID playerId, String normalized) {
        long now = System.currentTimeMillis();
        long cutoff = now - LobbySettings.CHAT_REPEAT_WINDOW_MS;
        List<Entry> list = RECENT.computeIfAbsent(playerId, k -> new ArrayList<>());
        synchronized (list) {
            Iterator<Entry> it = list.iterator();
            int same = 0;
            while (it.hasNext()) {
                Entry e = it.next();
                if (e.at < cutoff) {
                    it.remove();
                    continue;
                }
                if (e.text.equals(normalized)) same++;
            }
            list.add(new Entry(normalized, now));
            return same >= LobbySettings.CHAT_MAX_REPEAT;
        }
    }

    private record Entry(String text, long at) {}
}
