package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.friend.Friend;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aplica modo de visibilidade de outros jogadores no lobby.
 */
public final class PlayerVisibilityService {

    private static final Map<UUID, PlayerVisibilityMode> MODES = new ConcurrentHashMap<>();

    private PlayerVisibilityService() {}

    public static PlayerVisibilityMode get(Player player) {
        return MODES.getOrDefault(player.getUniqueId(), PlayerVisibilityMode.ALL);
    }

    public static void set(Player player, PlayerVisibilityMode mode) {
        MODES.put(player.getUniqueId(), mode);
        apply(player);
        HotbarLayout.applyDefault(player);
    }

    public static PlayerVisibilityMode cycle(Player player) {
        PlayerVisibilityMode next = get(player).next();
        set(player, next);
        return next;
    }

    public static void apply(Player viewer) {
        PlayerVisibilityMode mode = get(viewer);
        Account account = com.minecraft.lobbyhost.Core.getAccountController().of(viewer.getUniqueId());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(viewer)) continue;
            boolean show = switch (mode) {
                case ALL -> true;
                case NONE -> false;
                case FRIENDS_ONLY -> account != null && isFriend(account, other.getUniqueId());
            };
            if (show) {
                viewer.showPlayer(com.minecraft.lobbyhost.Core.getJavaPlugin(), other);
            } else {
                viewer.hidePlayer(com.minecraft.lobbyhost.Core.getJavaPlugin(), other);
            }
        }
    }

    public static void onPlayerJoin(Player joined) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(joined)) continue;
            apply(viewer);
        }
        apply(joined);
    }

    public static void onPlayerQuit(Player quit) {
        MODES.remove(quit.getUniqueId());
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(quit)) {
                viewer.showPlayer(com.minecraft.lobbyhost.Core.getJavaPlugin(), quit);
            }
        }
    }

    private static boolean isFriend(Account account, UUID otherId) {
        var meta = account.getFriendMetadata();
        if (meta == null || meta.getFriends() == null) return false;
        for (Friend f : meta.getFriends().values()) {
            if (f.getId() != null && f.getId().equals(otherId)) return true;
        }
        return false;
    }
}
