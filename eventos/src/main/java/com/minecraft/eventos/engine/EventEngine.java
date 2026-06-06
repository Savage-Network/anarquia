package com.minecraft.eventos.engine;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.arena.ArenaDefinition;
import com.minecraft.eventos.arena.ArenaGenerator;
import com.minecraft.eventos.arena.ArenaRepository;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.integration.NetworkLivePublisher;
import com.minecraft.eventos.game.Game;
import com.minecraft.eventos.mode.*;
import com.minecraft.eventos.scheduler.EventSchedulerService;
import com.minecraft.eventos.service.EventKitService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** Orquestrador central de eventos. */
public final class EventEngine {

    private final EventosPlugin plugin;
    private final Game session = new Game();
    private final EventModeRegistry modes = new EventModeRegistry();
    private final ArenaRepository arenas;
    private EventSchedulerService scheduler;

    public EventEngine(EventosPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new ArenaRepository(plugin);
        registerDefaultModes();
    }

    public void init() {
        arenas.load();
        modes.find("sumo").ifPresent(session::bindMode);
        scheduler = new EventSchedulerService(plugin, this);
        scheduler.start();
    }

    public void shutdown() {
        if (scheduler != null) scheduler.stop();
        NetworkLivePublisher.clear();
    }

    public void pulse() {
        session.pulse(this);
        if (scheduler != null) scheduler.tick();
    }

    private void registerDefaultModes() {
        modes.register(new SumoMode());
        modes.register(new SpleefMode());
        modes.register(new TntRunMode());
        modes.register(new BlockPartyMode());
        modes.register(new FfaMode());
    }

    public boolean setMode(String modeId) {
        if (session.getState() != GameState.WAITING) return false;
        return modes.find(modeId).map(mode -> {
            session.bindMode(mode);
            return true;
        }).orElse(false);
    }

    public boolean useArena(String arenaId) {
        return arenas.find(arenaId).map(arena -> {
            session.setArena(arena);
            session.setArenaId(arena.getId());
            Location spawn = arena.primarySpawn();
            if (spawn != null) session.setSpawn(spawn);
            if (session.getActiveMode() == null) {
                modes.find(arena.getModeId()).ifPresent(session::bindMode);
            }
            return true;
        }).orElse(false);
    }

    public ArenaDefinition generateMap(Player staff, String arenaId, String modeId, int radius) {
        ArenaDefinition arena = ArenaGenerator.generate(staff, arenaId, modeId, radius);
        arenas.save(arena);
        session.setArena(arena);
        session.setArenaId(arena.getId());
        Location spawn = arena.primarySpawn();
        if (spawn != null) session.setSpawn(spawn);
        setMode(modeId);
        return arena;
    }

    public void startEvent(Player host, int seconds, boolean automated, String taskId) {
        if (!session.canStart()) return;
        if (host != null) {
            session.setHostId(host.getUniqueId());
            session.setHostName(host.getName());
        }
        session.setAutomated(automated);
        session.setScheduledTaskId(taskId);
        session.requestStart(seconds);
    }

    public void resetSession(EventSession target) {
        target.prepareReset();
        target.sendAllToHub();
        NetworkLivePublisher.clear();
    }

    public void stopEvent(String reason) {
        if (session.getState() == GameState.WAITING) return;
        session.end(session.getWinnerId());
    }

    public void applyKit(Player player, String kitId) {
        EventKitService.apply(player, kitId);
    }

    public boolean joinQueue(Player player) {
        if (session.getState() != GameState.WAITING && session.getState() != GameState.STARTING) {
            return false;
        }
        session.getContext().getQueue().add(player.getUniqueId());
        session.getContext().registerParticipant(player.getUniqueId());
        return true;
    }

    public boolean leaveQueue(Player player) {
        return session.getContext().getQueue().remove(player.getUniqueId());
    }

    public Game getSession() {
        return session;
    }

    public EventModeRegistry getModes() {
        return modes;
    }

    public ArenaRepository getArenas() {
        return arenas;
    }

    public EventSchedulerService getScheduler() {
        return scheduler;
    }
}
