package com.minecraft.game.bukkit.staff;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.NetworkPresenceData;
import com.minecraft.game.backend.database.HikariProvider;
import com.minecraft.game.util.BungeeCordUtil;
import com.minecraft.game.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** /go — teleporta staff até um jogador em qualquer servidor da rede. */
public final class StaffGoService {

    private static final Map<UUID, UUID> PENDING = new ConcurrentHashMap<>();

    private StaffGoService() {}

    public static boolean canUse(Account account, Player player) {
        if (account != null && account.isStaffer()) return true;
        return player != null && player.hasPermission("game.go");
    }

    public static void go(Player staff, String targetName) {
        if (staff == null || targetName == null || targetName.isBlank()) return;

        Player local = Bukkit.getPlayerExact(targetName);
        if (local != null && local.isOnline()) {
            teleport(staff, local);
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                    + ">Teleportado até <#F1F5F9>" + local.getName() + "<" + ModernText.SUCCESS + ">."));
            return;
        }

        HikariProvider db = Core.getDatabase();
        if (db == null) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                    + ">Jogador offline ou não encontrado."));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            OptionalPresence lookup = lookup(db, targetName);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> finish(staff, lookup));
        });
    }

    private static void finish(Player staff, OptionalPresence lookup) {
        if (lookup == null) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                    + ">Jogador offline ou não encontrado."));
            return;
        }

        Player sameServer = Bukkit.getPlayer(lookup.targetId());
        if (sameServer != null && sameServer.isOnline()) {
            teleport(staff, sameServer);
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                    + ">Teleportado até <#F1F5F9>" + sameServer.getName() + "<" + ModernText.SUCCESS + ">."));
            return;
        }

        String here = GameSettings.SERVER_CHAT_ID;
        if (lookup.serverId().equalsIgnoreCase(here)) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                    + ">Jogador offline ou não encontrado."));
            return;
        }

        PENDING.put(staff.getUniqueId(), lookup.targetId());
        JavaPlugin plugin = GamePlugin.getInstance();
        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.WARN
                + ">Conectando ao servidor <#F1F5F9>" + lookup.serverId()
                + "<" + ModernText.WARN + "> para ir até <#F1F5F9>" + lookup.playerName() + "<" + ModernText.WARN + ">..."));
        BungeeCordUtil.connect(staff, plugin, lookup.serverId());
    }

    /** Chamado ao staff entrar no servidor de destino. */
    public static void tryCompletePending(Player staff) {
        UUID targetId = PENDING.remove(staff.getUniqueId());
        if (targetId == null) return;

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null && target.isOnline()) {
                teleport(staff, target);
                staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                        + ">Teleportado até <#F1F5F9>" + target.getName() + "<" + ModernText.SUCCESS + ">."));
            } else {
                staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                        + ">Não foi possível localizar o jogador neste servidor."));
            }
        }, 10L);
    }

    public static void cancel(UUID staffId) {
        if (staffId != null) PENDING.remove(staffId);
    }

    private static void teleport(Player staff, Player target) {
        staff.teleport(target.getLocation());
    }

    private static OptionalPresence lookup(HikariProvider db, String targetName) {
        try {
            return new NetworkPresenceData(db).findOnlineByName(targetName)
                    .map(p -> new OptionalPresence(p.playerId(), p.playerName(), p.serverId()))
                    .orElse(null);
        } catch (RuntimeException e) {
            Core.getLogger().warning("[go] " + e.getMessage());
            return null;
        }
    }

    private record OptionalPresence(UUID targetId, String playerName, String serverId) {}
}
