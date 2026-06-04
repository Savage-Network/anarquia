package com.minecraft.auth.bukkit.manager;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.account.context.objects.rank.Rank;
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
        account.refreshPermissions();

        PermissionAttachment attachment = player.addAttachment(Core.getJavaPlugin());
        ATTACHMENTS.put(player.getUniqueId(), attachment);
        Set<String> nodes = new LinkedHashSet<>();

        nodes.addAll(account.getRankType().getPermissionNodes());
        for (Rank rank : account.getAvailableRanks()) {
            if (rank != null && !rank.hasExpired() && rank.getType() != null) {
                nodes.addAll(rank.getType().getPermissionNodes());
            }
        }
        account.getPermissions().stream()
                .filter(p -> !p.hasExpired())
                .map(com.minecraft.auth.account.context.objects.permission.Permission::getKey)
                .forEach(nodes::add);

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
}
