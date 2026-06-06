package com.minecraft.eventos.scheduler;

import com.minecraft.eventos.config.EventConfig;
import com.minecraft.eventos.engine.EventEngine;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.integration.DiscordEventNotifier;
import com.minecraft.eventos.message.EventMessages;
import com.minecraft.eventos.EventosPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Agenda eventos automaticos conforme {@code config.yml > scheduler}. */
public final class EventSchedulerService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final EventosPlugin plugin;
    private final EventEngine engine;
    private final Map<String, ScheduledRun> activeRuns = new HashMap<>();
    private int taskId = -1;

    public EventSchedulerService(EventosPlugin plugin, EventEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    public void start() {
        if (taskId >= 0) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (taskId >= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        activeRuns.clear();
    }

    public void tick() {
        if (!EventConfig.isSchedulerEnabled()) return;
        if (engine.getSession().getState() != GameState.WAITING
                && engine.getSession().getState() != GameState.ENDING) {
            return;
        }

        ZoneId zone = ZoneId.of(EventConfig.getSchedulerTimezone());
        LocalTime now = LocalTime.now(zone).withSecond(0).withNano(0);
        String day = LocalDate.now(zone).getDayOfWeek().name().substring(0, 3).toUpperCase(Locale.ROOT);

        for (EventConfig.ScheduledTaskConfig task : EventConfig.scheduledTasks(plugin)) {
            if (!matchesDay(task.days(), day)) continue;

            for (String timeStr : task.times()) {
                LocalTime scheduled = parseTime(timeStr);
                if (scheduled == null) continue;

                String runKey = task.id() + "@" + timeStr;
                ScheduledRun run = activeRuns.get(runKey);

                if (run == null && now.equals(scheduled.minusSeconds(task.announceBeforeSec()))) {
                    beginAnnounce(task, runKey);
                } else if (run != null && run.phase == Phase.ANNOUNCED) {
                    run.tickSeconds++;
                    if (run.tickSeconds >= task.announceBeforeSec()) {
                        openQueue(task, run);
                    }
                } else if (run != null && run.phase == Phase.QUEUE_OPEN) {
                    run.tickSeconds++;
                    int players = Bukkit.getOnlinePlayers().size();
                    if (players >= task.minPlayers() && run.tickSeconds >= 10) {
                        startAutomated(task, run);
                    } else if (run.tickSeconds >= task.autoStartDelaySec()) {
                        if (players >= task.minPlayers()) {
                            startAutomated(task, run);
                        } else {
                            cancelRun(runKey, task.id(), "min-players");
                        }
                    }
                }
            }
        }
    }

    public void forceTask(String taskId, Player staff) {
        EventConfig.ScheduledTaskConfig task = EventConfig.scheduledTasks(plugin).stream()
                .filter(t -> t.id().equalsIgnoreCase(taskId))
                .findFirst()
                .orElse(null);
        if (task == null) return;

        String runKey = task.id() + "@force";
        beginAnnounce(task, runKey);
        ScheduledRun run = activeRuns.get(runKey);
        if (run != null) {
            openQueue(task, run);
            startAutomated(task, run);
        }
    }

    private void beginAnnounce(EventConfig.ScheduledTaskConfig task, String runKey) {
        ScheduledRun run = new ScheduledRun(task.id(), Phase.ANNOUNCED, 0);
        activeRuns.put(runKey, run);

        Bukkit.getOnlinePlayers().forEach(p ->
                EventMessages.sendPrefixed(p, "event.scheduler.announce", Map.of(
                        "mode", task.modeId(),
                        "seconds", String.valueOf(task.announceBeforeSec()),
                        "task", task.id()
                )));

        DiscordEventNotifier.scheduled(task.id(), task.modeId(), task.announceBeforeSec());
    }

    private void openQueue(EventConfig.ScheduledTaskConfig task, ScheduledRun run) {
        run.phase = Phase.QUEUE_OPEN;
        run.tickSeconds = 0;
        engine.setMode(task.modeId());

        if (task.generateMap() && (task.arenaId() == null || task.arenaId().isBlank())) {
            Player anchor = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            if (anchor != null) {
                String arenaId = "auto-" + task.modeId() + "-" + System.currentTimeMillis() % 100000;
                engine.generateMap(anchor, arenaId, task.modeId(), task.mapRadius());
            }
        } else if (task.arenaId() != null && !task.arenaId().isBlank()) {
            engine.useArena(task.arenaId());
        }

        Bukkit.getOnlinePlayers().forEach(p -> engine.joinQueue(p));

        Bukkit.getOnlinePlayers().forEach(p ->
                EventMessages.send(p, "event.scheduler.queue-open", Map.of(
                        "mode", task.modeId(),
                        "min", String.valueOf(task.minPlayers())
                )));
    }

    private void startAutomated(EventConfig.ScheduledTaskConfig task, ScheduledRun run) {
        run.phase = Phase.STARTED;
        activeRuns.values().removeIf(r -> r.taskId.equals(task.id()));

        int countdown = Math.min(EventConfig.getStartingCountdown(), 30);
        engine.startEvent(null, countdown, true, task.id());

        Bukkit.getOnlinePlayers().forEach(p ->
                EventMessages.send(p, "event.scheduler.starting", Map.of(
                        "mode", task.modeId(),
                        "seconds", String.valueOf(countdown)
                )));
    }

    private void cancelRun(String runKey, String taskId, String reason) {
        activeRuns.remove(runKey);
        Bukkit.getOnlinePlayers().forEach(p ->
                EventMessages.send(p, "event.scheduler.cancelled", Map.of(
                        "task", taskId,
                        "reason", reason
                )));
    }

    private static boolean matchesDay(List<String> days, String today) {
        if (days == null || days.isEmpty()) return true;
        for (String d : days) {
            if (d == null) continue;
            String norm = d.trim().toUpperCase(Locale.ROOT);
            if ("EVERY".equals(norm) || "ALL".equals(norm)) return true;
            if (norm.startsWith(today)) return true;
            try {
                if (DayOfWeek.valueOf(norm).name().startsWith(today)) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static LocalTime parseTime(String raw) {
        try {
            return LocalTime.parse(raw.trim(), TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private enum Phase { ANNOUNCED, QUEUE_OPEN, STARTED }

    private static final class ScheduledRun {
        final String taskId;
        Phase phase;
        int tickSeconds;

        ScheduledRun(String taskId, Phase phase, int tickSeconds) {
            this.taskId = taskId;
            this.phase = phase;
            this.tickSeconds = tickSeconds;
        }
    }
}
