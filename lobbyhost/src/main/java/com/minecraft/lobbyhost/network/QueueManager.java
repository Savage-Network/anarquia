package com.minecraft.lobbyhost.network;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.LobbySettings.NetworkMode;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.network.EventosAccessGuard;
import com.minecraft.lobbyhost.network.NetworkStateCache;
import com.minecraft.lobbyhost.util.BungeeCordUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fila de conexão por modo de jogo.
 *
 * <p>Jogadores entram com {@link #join}, saem com {@link #leave}.
 * A cada tick de flush (chamado pelo TaskManager) o próximo da fila
 * é enviado ao servidor via BungeeCord.
 *
 * <p>Posição na fila é exibida via action bar a cada 2s.
 */
public final class QueueManager {

    /** Jogador → id do modo em que está na fila. */
    private static final Map<UUID, String> playerMode   = new ConcurrentHashMap<>();
    /** Modo → fila FIFO de UUIDs. */
    private static final Map<String, Queue<UUID>> queues = new ConcurrentHashMap<>();

    private QueueManager() {}

    // -------------------------------------------------------------------------

    /** Entra na fila de um modo. Sai da fila anterior se estiver em outra. */
    public static void join(Player player, String modeId) {
        NetworkMode mode = findMode(modeId);
        if (mode == null) {
            player.sendMessage("§cModo §e" + modeId + " §cnão encontrado.");
            return;
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (NetworkStateCache.isMaintenance()
                && !NetworkStateCache.canJoinDuringMaintenance(account)) {
            player.sendMessage("§cA rede está em manutenção. Apenas staff autorizado pode entrar nos modos.");
            return;
        }
        if (EventosAccessGuard.isBlockedMode(mode.id())) {
            EventosAccessGuard.sendBlocked(player);
            return;
        }

        // Já está nessa fila?
        if (modeId.equalsIgnoreCase(playerMode.get(player.getUniqueId()))) {
            int pos = position(player, modeId);
            player.sendMessage(Component.text("§eVocê já está na fila de §f" + displayName(mode)
                    + " §e— posição §f" + pos));
            return;
        }

        leave(player, false); // remove fila anterior silenciosamente

        queues.computeIfAbsent(modeId, k -> new LinkedList<>()).add(player.getUniqueId());
        playerMode.put(player.getUniqueId(), modeId);

        int pos = position(player, modeId);
        player.sendMessage(Component.text("§aEntrou na fila de " + displayName(mode)
                + " §a— posição §f" + pos));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    /** Remove o jogador de qualquer fila que estiver. */
    public static void leave(Player player, boolean notify) {
        if (player == null) return;
        String mode = playerMode.remove(player.getUniqueId());
        if (mode == null) return;
        Queue<UUID> q = queues.get(mode);
        if (q != null) q.remove(player.getUniqueId());
        if (notify) {
            player.sendMessage("§eSaiu da fila de §f" + mode + "§e.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        }
    }

    /** Posição 1-indexed do jogador na fila, ou -1 se não estiver. */
    public static int position(Player player, String modeId) {
        Queue<UUID> q = queues.get(modeId);
        if (q == null) return -1;
        int i = 1;
        for (UUID id : q) {
            if (id.equals(player.getUniqueId())) return i;
            i++;
        }
        return -1;
    }

    /** Retorna o id do modo em que o jogador está na fila, ou null. */
    public static String currentMode(Player player) {
        return playerMode.get(player.getUniqueId());
    }

    /**
     * Chamado periodicamente (a cada ~2s pelo TaskManager).
     * Envia o próximo da fila de cada modo ao servidor correspondente.
     */
    public static void flush() {
        for (NetworkMode mode : LobbySettings.NETWORK_MODES) {
            Queue<UUID> q = queues.get(mode.id());
            if (q == null || q.isEmpty()) continue;

            UUID next = q.poll();
            if (next == null) continue;
            playerMode.remove(next);

            Player player = Bukkit.getPlayer(next);
            if (player == null || !player.isOnline()) continue; // offline: descarta

            player.sendMessage(Component.text("§aConectando ao servidor §f" + displayName(mode) + "§a..."));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            BungeeCordUtil.connect(player, Core.getJavaPlugin(), mode.bungeeServer());
        }
    }

    /**
     * Envia action bar de posição para todos na fila.
     * Chamado a cada ~2s.
     */
    public static void broadcastPositions() {
        for (Map.Entry<UUID, String> entry : playerMode.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            String modeId = entry.getValue();
            NetworkMode mode = findMode(modeId);
            if (mode == null) continue;
            int pos  = position(player, modeId);
            int total = queues.getOrDefault(modeId, new LinkedList<>()).size();
            player.sendActionBar(Component.text(
                "§e⏳ Fila §f" + displayName(mode) + " §8| §ePosição §f" + pos + "§7/§f" + total
            ));
        }
    }

    // ---- helpers ----

    public static NetworkMode findMode(String id) {
        if (id == null) return null;
        for (NetworkMode m : LobbySettings.NETWORK_MODES) {
            if (m.id().equalsIgnoreCase(id) || m.bungeeServer().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    private static String displayName(NetworkMode mode) {
        // strip MiniMessage tags para mensagem legacy
        return mode.displayName().replaceAll("<[^>]+>", "");
    }
}
