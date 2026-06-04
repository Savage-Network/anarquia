package com.minecraft.eventos.game;

import com.minecraft.eventos.Core;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.game.team.TeamPresetType;
import com.minecraft.eventos.game.variable.Variable;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maquina de estados global do evento. Port de {@code Game} do modulo
 * Eventos do Savage.
 *
 * <p>Fluxo: {@code WAITING -> STARTING -> PLAYING -> ENDING -> reset()}.
 */
@Getter
@Setter
public final class Game {

    private final long id = System.currentTimeMillis();

    private GameState state = GameState.WAITING;
    private Location spawn;
    private Location dieSpawnPoint;
    private UUID winnerId;

    /** Tempo em segundos no estado atual. */
    private final AtomicInteger time = new AtomicInteger(0);

    private boolean sumo = false;
    private boolean team = false;
    private boolean teamSelect = false;
    private boolean spec = true;
    private boolean die = true;

    private final Map<UUID, TeamPresetType> teams = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();

    public List<Player> alives() {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isSpectator(p)) list.add(p);
        }
        return list;
    }

    public boolean isSpectator(Player player) {
        return player.getGameMode() == org.bukkit.GameMode.SPECTATOR;
    }

    public int minPlayers() { return Variable.getInt(Variable.Key.MIN_PLAYERS); }
    public int maxPlayers() { return Variable.getInt(Variable.Key.MAX_PLAYERS); }
    public int startCountdown() { return Variable.getInt(Variable.Key.STARTING_COUNTDOWN); }
    public int endCountdown() { return Variable.getInt(Variable.Key.ENDING_COUNTDOWN); }

    /* ------------------------------------------------------------ */

    public void broadcast(String legacy) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(legacy)));
    }

    public boolean canStart() {
        if (state != GameState.WAITING) return false;
        return alives().size() >= minPlayers();
    }

    public void requestStart(int seconds) {
        if (state != GameState.WAITING) return;
        state = GameState.STARTING;
        time.set(Math.max(1, seconds));
        broadcast("§6§lSAVAGE §7» §aEvento iniciando em §e" + seconds + "s§a!");
    }

    public void pulse() {
        switch (state) {
            case WAITING -> { /* sem timer ativo */ }
            case STARTING -> {
                int t = time.decrementAndGet();
                if (t <= 0) {
                    play();
                } else if (t % 5 == 0 || t <= 5) {
                    broadcast("§7Inicio em §e" + t + "s§7...");
                }
            }
            case PLAYING -> time.incrementAndGet();
            case ENDING -> {
                int t = time.decrementAndGet();
                if (t <= 0) reset();
            }
        }
    }

    private void play() {
        state = GameState.PLAYING;
        time.set(0);
        broadcast("§a§lO evento comecou!");
        if (spawn != null) {
            alives().forEach(p -> p.teleport(spawn));
        }
    }

    public void end(UUID winnerId) {
        this.winnerId = winnerId;
        this.state = GameState.ENDING;
        this.time.set(endCountdown());

        Player winner = winnerId != null ? Bukkit.getPlayer(winnerId) : null;
        broadcast("§6§lFIM DO EVENTO!");
        if (winner != null) {
            broadcast("§6§lVencedor: §e" + winner.getName());
        }
    }

    public void reset() {
        state = GameState.WAITING;
        time.set(0);
        winnerId = null;
        kills.clear();
        teams.clear();
        sumo = false;
        team = false;
        teamSelect = false;

        // Devolver todos ao hub
        String target = com.minecraft.eventos.EventosSettings.POST_END_TARGET;
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
                java.io.DataOutputStream out = new java.io.DataOutputStream(b);
                out.writeUTF("Connect");
                out.writeUTF(target);
                p.sendPluginMessage(Core.getJavaPlugin(), "BungeeCord", b.toByteArray());
            } catch (Throwable ignored) {
                if (spawn != null) p.teleport(spawn);
            }
        }
        broadcast("§7Evento reiniciado. Aguardando jogadores...");
    }
}
