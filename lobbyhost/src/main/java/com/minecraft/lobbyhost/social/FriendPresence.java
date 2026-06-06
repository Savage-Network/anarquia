package com.minecraft.lobbyhost.social;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.friend.Friend;
import com.minecraft.lobbyhost.account.context.objects.friend.FriendMetadata;
import com.minecraft.lobbyhost.backend.data.NetworkPresenceData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class FriendPresence {

    private FriendPresence() {}

    public record Status(boolean online, String serverId, String serverDisplay) {}

    public static Status resolve(UUID friendId) {
        if (friendId == null) return offline();
        Player local = Bukkit.getPlayer(friendId);
        if (local != null && local.isOnline()) {
            return new Status(true, "lobby", formatServerId("lobby"));
        }
        NetworkPresenceData data = Core.getNetworkPresenceData();
        if (data == null) return offline();
        return data.findOnline(friendId)
                .map(p -> new Status(true, p.serverId(), formatServerId(p.serverId())))
                .orElseGet(FriendPresence::offline);
    }

    public static Map<UUID, Status> resolveMany(List<Friend> friends) {
        Map<UUID, Status> out = new HashMap<>();
        if (friends == null || friends.isEmpty()) return out;

        List<UUID> lookup = new ArrayList<>();
        for (Friend friend : friends) {
            if (friend == null || friend.getId() == null) continue;
            Player local = Bukkit.getPlayer(friend.getId());
            if (local != null && local.isOnline()) {
                out.put(friend.getId(), new Status(true, "lobby", formatServerId("lobby")));
            } else {
                lookup.add(friend.getId());
            }
        }

        if (lookup.isEmpty()) return out;

        NetworkPresenceData data = Core.getNetworkPresenceData();
        if (data == null) {
            lookup.forEach(id -> out.putIfAbsent(id, offline()));
            return out;
        }

        Map<UUID, NetworkPresenceData.Presence> online = data.findOnlineByIds(lookup);
        for (UUID id : lookup) {
            NetworkPresenceData.Presence presence = online.get(id);
            if (presence != null) {
                out.put(id, new Status(true, presence.serverId(), formatServerId(presence.serverId())));
            } else {
                out.put(id, offline());
            }
        }
        return out;
    }

    public static int countFriendsOnlineOnNetwork(Account account) {
        if (account == null) return 0;
        FriendMetadata meta = account.getFriendMetadata();
        if (meta == null || meta.getFriends() == null || meta.getFriends().isEmpty()) return 0;
        return resolveMany(new ArrayList<>(meta.getFriends().values())).values().stream()
                .filter(Status::online)
                .mapToInt(s -> 1)
                .sum();
    }

    public static String formatServerId(String serverId) {
        if (serverId == null || serverId.isBlank()) return "Rede";
        String key = serverId.toLowerCase(Locale.ROOT);
        for (LobbySettings.NetworkMode mode : LobbySettings.NETWORK_MODES) {
            if (mode.bungeeServer().equalsIgnoreCase(key) || mode.heartbeatId().equalsIgnoreCase(key)) {
                return stripMiniTags(mode.displayName());
            }
        }
        return switch (key) {
            case "lobby", "lobbyhost" -> "Lobby";
            case "auth" -> "Auth";
            case "proxy" -> "Rede";
            default -> capitalize(key);
        };
    }

    private static Status offline() {
        return new Status(false, null, null);
    }

    private static String capitalize(String word) {
        if (word == null || word.isBlank()) return "Rede";
        if (word.length() == 1) return word.toUpperCase(Locale.ROOT);
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String stripMiniTags(String mini) {
        if (mini == null) return "Rede";
        return mini.replaceAll("<[^>]+>", "").trim();
    }
}
