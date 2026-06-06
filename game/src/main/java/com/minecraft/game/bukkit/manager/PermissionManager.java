package com.minecraft.game.bukkit.manager;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.context.objects.rank.Rank;
import com.minecraft.game.account.context.objects.rank.RankType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sincroniza permissoes da {@link Account} (rank + extras) com o Bukkit.
 */
public final class PermissionManager {

    private static final ConcurrentHashMap<UUID, PermissionAttachment> ATTACHMENTS = new ConcurrentHashMap<>();

    private PermissionManager() {}

    public static void loadPermissions(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        unloadPermissions(player);
        account.refreshPermissions();

        PermissionAttachment attachment = player.addAttachment(Core.getJavaPlugin());
        ATTACHMENTS.put(player.getUniqueId(), attachment);
        Set<String> nodes = new LinkedHashSet<>();

        collectRankNodes(account.getRankType(), nodes);
        for (Rank rank : account.getAvailableRanks()) {
            if (rank != null && !rank.hasExpired() && rank.getType() != null) {
                collectRankNodes(rank.getType(), nodes);
            }
        }
        account.getPermissions().stream()
                .filter(p -> !p.hasExpired())
                .map(com.minecraft.game.account.context.objects.permission.Permission::getKey)
                .forEach(nodes::add);

        for (Tag tag : Tag.values()) {
            if (!account.hasTag(tag)) continue;
            nodes.add(tag.getPermission());
            if (tag.isStaffTag()) {
                nodes.addAll(Tag.STAFF_CHAT_NODES);
            }
        }

        expandZwNodes(account, nodes);

        for (String node : nodes) {
            if (node != null && !node.isEmpty()) {
                attachment.setPermission(node, true);
            }
        }
    }

    private static void collectRankNodes(RankType type, Set<String> nodes) {
        nodes.addAll(type.getPermissionNodes());
    }

    private static void expandZwNodes(Account account, Set<String> nodes) {
        if (!hasWildcardRank(account)) return;
        nodes.add("*");
        nodes.add("game.*");
        nodes.add("lobbyhost.*");
        nodes.add("proxyhost.*");
        nodes.add("auth.*");
        nodes.add("eventos.*");
        nodes.add("tag.*");
        nodes.add("medal.*");
        for (org.bukkit.permissions.Permission perm : Bukkit.getPluginManager().getPermissions()) {
            nodes.add(perm.getName());
        }
    }

    private static boolean hasWildcardRank(Account account) {
        if (account.getRankType().hasWildcardAccess()) return true;
        for (Rank rank : account.getAvailableRanks()) {
            if (rank != null && !rank.hasExpired()
                    && rank.getType() != null && rank.getType().hasWildcardAccess()) {
                return true;
            }
        }
        for (Tag tag : Tag.values()) {
            if (tag.hasWildcardAccess() && account.hasTag(tag)) return true;
        }
        return account.getPermissions().stream()
                .anyMatch(p -> !p.hasExpired() && Tag.isWildcardPermissionKey(p.getKey()));
    }

    public static void unloadPermissions(Player player) {
        PermissionAttachment attachment = ATTACHMENTS.remove(player.getUniqueId());
        if (attachment != null) {
            attachment.remove();
        }
    }
}
