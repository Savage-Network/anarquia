package com.minecraft.lobbyhost.social;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.util.BungeeCordUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Party leve em memoria no lobby (lider = UUID do lider). */
public final class PartyService {

    private static final Map<UUID, UUID> MEMBER_TO_LEADER = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<UUID>> LEADER_TO_MEMBERS = new ConcurrentHashMap<>();

    private PartyService() {}

    public static UUID leaderOf(UUID member) {
        return MEMBER_TO_LEADER.get(member);
    }

    public static boolean inParty(UUID player) {
        return MEMBER_TO_LEADER.containsKey(player);
    }

    public static Set<UUID> members(UUID leader) {
        return LEADER_TO_MEMBERS.getOrDefault(leader, Collections.emptySet());
    }

    public static void create(Player leader) {
        UUID id = leader.getUniqueId();
        if (inParty(id)) {
            leader.sendMessage(net.kyori.adventure.text.Component.text("§eVoce ja esta em uma party."));
            return;
        }
        LEADER_TO_MEMBERS.put(id, ConcurrentHashMap.newKeySet());
        MEMBER_TO_LEADER.put(id, id);
        leader.sendMessage(net.kyori.adventure.text.Component.text("§aParty criada! Convide com §e/party convidar <nick>"));
    }

    public static void leave(Player player) {
        UUID id = player.getUniqueId();
        UUID leader = MEMBER_TO_LEADER.remove(id);
        if (leader == null) {
            player.sendMessage(net.kyori.adventure.text.Component.text("§cVoce nao esta em uma party."));
            return;
        }
        Set<UUID> group = LEADER_TO_MEMBERS.get(leader);
        if (group != null) group.remove(id);
        if (leader.equals(id)) {
            disband(leader);
            player.sendMessage(net.kyori.adventure.text.Component.text("§cParty dissolvida."));
        } else {
            player.sendMessage(net.kyori.adventure.text.Component.text("§7Voce saiu da party."));
            broadcastToParty(leader, "§7" + player.getName() + " saiu da party.");
        }
    }

    public static void invite(Account inviter, String targetName) {
        Player ip = inviter.player();
        if (ip == null) return;
        UUID leaderId = leaderOf(inviter.getId());
        if (leaderId == null) {
            create(ip);
            leaderId = inviter.getId();
        }
        final UUID leaderFinal = leaderId;
        if (!leaderFinal.equals(inviter.getId())) {
            inviter.send("§cApenas o lider pode convidar.");
            return;
        }
        Set<UUID> group = LEADER_TO_MEMBERS.computeIfAbsent(leaderFinal, k -> ConcurrentHashMap.newKeySet());
        if (group.size() >= LobbySettings.PARTY_MAX_SIZE - 1) {
            inviter.send("§cParty cheia (max " + LobbySettings.PARTY_MAX_SIZE + ").");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            var opt = Core.getAccountData().loadByName(targetName);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (opt.isEmpty()) {
                        inviter.send("§cJogador nao encontrado.");
                        return;
                    }
                    UUID tid = opt.get().getId();
                    if (tid.equals(inviter.getId())) {
                        inviter.send("§cVoce nao pode convidar a si mesmo.");
                        return;
                    }
                    Account targetOnline = Core.getAccountController().of(tid);
                    if (targetOnline != null && !targetOnline.getToggle().isAllowPartyInvites()) {
                        inviter.send("§cEsse jogador nao aceita convites de party.");
                        return;
                    }
                    Player tp = Bukkit.getPlayer(tid);
                    if (tp == null || !tp.isOnline()) {
                        inviter.send("§cO jogador precisa estar online no lobby.");
                        return;
                    }
                    if (inParty(tid)) {
                        inviter.send("§cEsse jogador ja esta em uma party.");
                        return;
                    }
                    group.add(tid);
                    MEMBER_TO_LEADER.put(tid, leaderFinal);
                    inviter.send("§a" + opt.get().getName() + " entrou na party!");
                    tp.sendMessage(net.kyori.adventure.text.Component.text(
                            "§aVoce entrou na party de §f" + inviter.getNickname() + "§a."));
            });
        });
    }

    /** Publica no Redis para todos os membros online no lobby conectarem no mesmo modo. */
    public static void connectParty(Player leader, String bungeeServer) {
        UUID leaderId = leader.getUniqueId();
        if (!leaderId.equals(leaderOf(leaderId))) {
            leader.sendMessage(net.kyori.adventure.text.Component.text("§cApenas o lider pode escolher o servidor."));
            return;
        }
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) {
            BungeeCordUtil.connect(leader, Core.getJavaPlugin(), bungeeServer);
            return;
        }
        Core.getRedis().publish(Constant.REDIS_PARTY_CONNECT_CHANNEL,
                leaderId + "|" + bungeeServer);
        BungeeCordUtil.connect(leader, Core.getJavaPlugin(), bungeeServer);
    }

    public static void onPartyConnectMessage(String message) {
        String[] parts = message.split("\\|", 2);
        if (parts.length < 2) return;
        try {
            UUID leaderId = UUID.fromString(parts[0].trim());
            String server = parts[1].trim();
            var plugin = Core.getJavaPlugin();
            for (UUID member : allMembers(leaderId)) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline() && !p.getUniqueId().equals(leaderId)) {
                    BungeeCordUtil.connect(p, plugin, server);
                    p.sendMessage(net.kyori.adventure.text.Component.text(
                            "§aParty: conectando em §f" + server + "§a..."));
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static void onQuit(Player player) {
        UUID id = player.getUniqueId();
        UUID leader = MEMBER_TO_LEADER.remove(id);
        if (leader == null) return;
        Set<UUID> group = LEADER_TO_MEMBERS.get(leader);
        if (group != null) group.remove(id);
        if (leader.equals(id)) {
            disband(leader);
        } else {
            broadcastToParty(leader, "§7" + player.getName() + " saiu (offline).");
        }
    }

    private static Set<UUID> allMembers(UUID leader) {
        Set<UUID> set = LEADER_TO_MEMBERS.get(leader);
        if (set == null) return Set.of(leader);
        Set<UUID> all = ConcurrentHashMap.newKeySet();
        all.add(leader);
        all.addAll(set);
        return all;
    }

    private static void disband(UUID leader) {
        Set<UUID> group = LEADER_TO_MEMBERS.remove(leader);
        if (group != null) {
            for (UUID m : group) MEMBER_TO_LEADER.remove(m);
        }
        MEMBER_TO_LEADER.remove(leader);
    }

    private static void broadcastToParty(UUID leader, String msg) {
        for (UUID id : allMembers(leader)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.sendMessage(net.kyori.adventure.text.Component.text(msg));
        }
    }
}
