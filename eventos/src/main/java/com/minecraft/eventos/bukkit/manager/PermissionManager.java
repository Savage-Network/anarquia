package com.minecraft.eventos.bukkit.manager;

import com.minecraft.eventos.Core;
import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.account.context.objects.rank.RankType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PermissionManager {

    private static final ConcurrentHashMap<UUID, PermissionAttachment> ATTACHMENTS = new ConcurrentHashMap<>();

    private PermissionManager() {}

    public static void loadPermissions(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        unloadPermissions(player);
        Set<String> nodes = new LinkedHashSet<>();
        nodes.addAll(account.getRankType().getPermissionNodes());

        if (hasZwRank(account)) {
            nodes.add("*");
            nodes.add("eventos.*");
            nodes.add("game.*");
            nodes.add("lobbyhost.*");
            nodes.add("proxyhost.*");
            nodes.add("auth.*");
            nodes.add("tag.*");
            nodes.add("medal.*");
            for (org.bukkit.permissions.Permission perm : Bukkit.getPluginManager().getPermissions()) {
                nodes.add(perm.getName());
            }
        }

        PermissionAttachment attachment = player.addAttachment(Core.getJavaPlugin());
        ATTACHMENTS.put(player.getUniqueId(), attachment);
        for (String node : nodes) {
            if (node != null && !node.isEmpty()) {
                attachment.setPermission(node, true);
            }
        }
    }

    public static void unloadPermissions(Player player) {
        PermissionAttachment attachment = ATTACHMENTS.remove(player.getUniqueId());
        if (attachment != null) {
            attachment.remove();
        }
    }

    private static boolean hasZwRank(Account account) {
        return account.getRankType() == RankType.ZW;
    }
}
