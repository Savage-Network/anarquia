package com.minecraft.eventos.engine;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.arena.ArenaDefinition;
import com.minecraft.eventos.config.EventConfig;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.game.team.TeamPresetType;
import com.minecraft.eventos.game.variable.Variable;
import com.minecraft.eventos.integration.DiscordEventNotifier;
import com.minecraft.eventos.integration.NetworkLivePublisher;
import com.minecraft.eventos.message.EventMessages;
import com.minecraft.eventos.service.EventRewardService;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maquina de estados de uma sessao de evento.
 * Substitui a logica central de {@link com.minecraft.eventos.game.Game}.
 */
@Getter
@Setter
public class EventSession {

    private final long id = System.currentTimeMillis();
    private final EventContext context = new EventContext();

    private GameState state = GameState.WAITING;
    private Location spawn;
    private Location dieSpawnPoint;
    private UUID winnerId;
    private TeamPresetType winningTeam;

    private final AtomicInteger time = new AtomicInteger(0);

    private String modeId = "sumo";
    private EventMode activeMode;
    private ArenaDefinition arena;
    private String arenaId;

    private boolean automated;
    private String scheduledTaskId;
    private UUID hostId;
    private String hostName;

    /** Compat legado */
    private boolean sumo;
    private boolean team;
    private boolean teamSelect;

    public List<Player> alives() {
        List<Player> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isSpectator(p)) list.add(p);
        }
        return list;
    }

    public List<Player> aliveParticipants() {
        List<Player> list = new ArrayList<>();
        for (Player p : alives()) {
            if (context.isParticipant(p.getUniqueId())) list.add(p);
        }
        return list;
    }

    public boolean isSpectator(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR;
    }

    public int minPlayers() {
        return Variable.getInt(Variable.Key.MIN_PLAYERS);
    }

    public int maxPlayers() {
        return Variable.getInt(Variable.Key.MAX_PLAYERS);
    }

    public int startCountdown() {
        return Variable.getInt(Variable.Key.STARTING_COUNTDOWN);
    }

    public int endCountdown() {
        return Variable.getInt(Variable.Key.ENDING_COUNTDOWN);
    }

    public EventModeRules activeRules() {
        return activeMode != null ? activeMode.rules() : EventModeRules.pvpArena();
    }

    public void broadcast(String messageKey, java.util.Map<String, String> placeholders) {
        Component msg = EventMessages.component(messageKey, placeholders);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    public void broadcastRaw(Component component) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
    }

    /** Compat legado com § strings */
    public void broadcast(String legacy) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(legacy)));
    }

    public boolean canStart() {
        if (state != GameState.WAITING) return false;
        int count = automated ? Math.max(alives().size(), context.getQueue().size()) : alives().size();
        return count >= minPlayers();
    }

    public void bindMode(EventMode mode) {
        this.activeMode = mode;
        if (mode != null) {
            this.modeId = mode.id();
            this.sumo = "sumo".equalsIgnoreCase(mode.id());
            mode.onSessionBound(this);
        }
    }

    public void registerAllOnlineAsParticipants() {
        for (Player p : alives()) {
            context.registerParticipant(p.getUniqueId());
        }
        context.getQueue().forEach(context::registerParticipant);
    }

    public void requestStart(int seconds) {
        if (state != GameState.WAITING) return;
        state = GameState.STARTING;
        time.set(Math.max(1, seconds));
        registerAllOnlineAsParticipants();
        broadcast("event.session.starting", java.util.Map.of(
                "seconds", String.valueOf(seconds),
                "mode", activeMode != null ? activeMode.displayName() : modeId
        ));
        NetworkLivePublisher.publish(this);
        if (!automated) {
            DiscordEventNotifier.started(this);
        }
    }

    public void pulse(EventEngine engine) {
        switch (state) {
            case WAITING -> { /* idle */ }
            case STARTING -> {
                int t = time.decrementAndGet();
                if (t <= 0) {
                    goLive(engine);
                } else if (t % 5 == 0 || t <= 5) {
                    broadcast("event.session.countdown", java.util.Map.of("seconds", String.valueOf(t)));
                }
            }
            case PLAYING -> {
                int live = time.incrementAndGet();
                if (activeMode != null) {
                    activeMode.onTick(this, live);
                    checkWin(engine);
                }
            }
            case ENDING -> {
                int t = time.decrementAndGet();
                if (t <= 0) {
                    engine.resetSession(this);
                }
            }
        }
    }

    private void goLive(EventEngine engine) {
        state = GameState.PLAYING;
        time.set(0);
        Location tp = spawn != null ? spawn : (arena != null ? arena.primarySpawn() : null);
        if (tp != null) {
            this.spawn = tp;
            alives().forEach(p -> {
                p.setGameMode(GameMode.SURVIVAL);
                p.teleport(tp);
                engine.applyKit(p, activeMode != null ? activeMode.defaultKitId() : modeId);
            });
        }
        broadcast("event.session.live", java.util.Map.of(
                "mode", activeMode != null ? activeMode.displayName() : modeId
        ));
        if (activeMode != null) activeMode.onLive(this);
        NetworkLivePublisher.publish(this);
    }

    private void checkWin(EventEngine engine) {
        if (activeMode == null) return;
        var teamWin = activeMode.checkWinningTeam(this);
        if (teamWin.isPresent()) {
            winningTeam = teamWin.get();
            end(null);
            return;
        }
        var win = activeMode.checkWinner(this);
        win.ifPresent(uuid -> end(uuid));
    }

    public void eliminate(Player player, String reason, EventEngine engine) {
        if (player == null || isSpectator(player)) return;
        player.setGameMode(GameMode.SPECTATOR);
        if (dieSpawnPoint != null) player.teleport(dieSpawnPoint);
        broadcast("event.session.eliminated", java.util.Map.of(
                "player", player.getName(),
                "reason", reason != null ? reason : ""
        ));
        if (activeMode != null) activeMode.onEliminate(this, player, reason);
        if (engine != null) checkWin(engine);
    }

    /** @deprecated use {@link #eliminate(Player, String, EventEngine)} */
    public void eliminate(Player player, String reason) {
        eliminate(player, reason, EventosPlugin.getInstance().getEventEngine());
    }

    public void end(UUID winner) {
        this.winnerId = winner;
        this.state = GameState.ENDING;
        this.time.set(endCountdown());

        if (winner != null) {
            Player w = Bukkit.getPlayer(winner);
            broadcast("event.session.winner", java.util.Map.of(
                    "player", w != null ? w.getName() : "?"
            ));
        } else if (winningTeam != null) {
            broadcast("event.session.winner-team", java.util.Map.of(
                    "team", winningTeam.name()
            ));
        } else {
            broadcast("event.session.ended", java.util.Map.of());
        }

        EventRewardService.distribute(this);
        DiscordEventNotifier.ended(this);
        NetworkLivePublisher.publish(this);
        if (activeMode != null) activeMode.onEnd(this);
    }

    public void prepareReset() {
        state = GameState.WAITING;
        time.set(0);
        winnerId = null;
        winningTeam = null;
        context.reset();
        sumo = false;
        team = false;
        teamSelect = false;
        automated = false;
        scheduledTaskId = null;
        hostId = null;
        hostName = null;
    }

    public void sendAllToHub() {
        String target = EventConfig.getPostEndTarget();
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
                java.io.DataOutputStream out = new java.io.DataOutputStream(b);
                out.writeUTF("Connect");
                out.writeUTF(target);
                p.sendPluginMessage(EventosPlugin.getInstance(), "BungeeCord", b.toByteArray());
            } catch (Throwable ignored) {
                if (spawn != null) p.teleport(spawn);
            }
        }
        broadcast("event.session.reset", java.util.Map.of());
    }
}
