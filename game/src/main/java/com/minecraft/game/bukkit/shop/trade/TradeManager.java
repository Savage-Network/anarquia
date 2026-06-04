package com.minecraft.game.bukkit.shop.trade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TradeManager {

    private static final Map<UUID, UUID> PENDING_INVITES = new ConcurrentHashMap<>();
    private static final Map<UUID, TradeSession> ACTIVE = new ConcurrentHashMap<>();

    private TradeManager() {}

    public static void invite(Player from, Player to) {
        PENDING_INVITES.put(to.getUniqueId(), from.getUniqueId());
        to.sendMessage("§e" + from.getName() + " §7convidou para troca. §f/trade aceitar §7ou §f/trade recusar");
        from.sendMessage("§aConvite enviado para §f" + to.getName());
    }

    public static boolean accept(Player to) {
        UUID fromId = PENDING_INVITES.remove(to.getUniqueId());
        if (fromId == null) return false;
        Player from = Bukkit.getPlayer(fromId);
        if (from == null || !from.isOnline()) return false;

        TradeSession session = new TradeSession(UUID.randomUUID(), from.getUniqueId(), to.getUniqueId());
        ACTIVE.put(from.getUniqueId(), session);
        ACTIVE.put(to.getUniqueId(), session);

        new com.minecraft.game.bukkit.shop.gui.TradeMenu(from, session, true).open();
        new com.minecraft.game.bukkit.shop.gui.TradeMenu(to, session, false).open();
        return true;
    }

    public static void deny(Player to) {
        UUID fromId = PENDING_INVITES.remove(to.getUniqueId());
        if (fromId != null) {
            Player from = Bukkit.getPlayer(fromId);
            if (from != null) from.sendMessage("§c" + to.getName() + " recusou a troca.");
        }
    }

    public static TradeSession session(Player player) {
        return ACTIVE.get(player.getUniqueId());
    }

    public static void end(TradeSession session) {
        ACTIVE.remove(session.playerA());
        ACTIVE.remove(session.playerB());
    }
}
