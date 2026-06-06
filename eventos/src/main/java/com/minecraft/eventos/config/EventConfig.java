package com.minecraft.eventos.config;

import com.minecraft.eventos.EventosPlugin;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/** Configuracao de eventos e scheduler ({@code config.yml > event}). */
public final class EventConfig {

    @Getter private static String postEndTarget = "lobby";
    @Getter private static int defaultMinPlayers = 2;
    @Getter private static int defaultMaxPlayers = 50;
    @Getter private static int startingCountdown = 30;
    @Getter private static int endingCountdown = 10;
    @Getter private static int winnerGold = 500;
    @Getter private static int participantGold = 50;
    @Getter private static boolean schedulerEnabled = true;
    @Getter private static String schedulerTimezone = "America/Sao_Paulo";
    @Getter private static int autoMapRadius = 14;
    @Getter private static boolean autoGenerateMap = true;

    private EventConfig() {}

    public static void load(EventosPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection event = cfg.getConfigurationSection("event");
        if (event != null) {
            postEndTarget = event.getString("post-end-target", postEndTarget);
            defaultMinPlayers = event.getInt("min-players-default", defaultMinPlayers);
            defaultMaxPlayers = event.getInt("max-players-default", defaultMaxPlayers);
            startingCountdown = event.getInt("starting-countdown", startingCountdown);
            endingCountdown = event.getInt("ending-countdown", endingCountdown);
            autoMapRadius = event.getInt("auto-map-radius", autoMapRadius);
            autoGenerateMap = event.getBoolean("auto-generate-map", autoGenerateMap);
            ConfigurationSection rewards = event.getConfigurationSection("rewards");
            if (rewards != null) {
                winnerGold = rewards.getInt("winner-gold", winnerGold);
                participantGold = rewards.getInt("participant-gold", participantGold);
            }
        }
        ConfigurationSection sched = cfg.getConfigurationSection("scheduler");
        if (sched != null) {
            schedulerEnabled = sched.getBoolean("enabled", schedulerEnabled);
            schedulerTimezone = sched.getString("timezone", schedulerTimezone);
        }
    }

    public static List<ScheduledTaskConfig> scheduledTasks(EventosPlugin plugin) {
        List<ScheduledTaskConfig> out = new ArrayList<>();
        ConfigurationSection tasks = plugin.getConfig().getConfigurationSection("scheduler.tasks");
        if (tasks == null) return out;
        for (String id : tasks.getKeys(false)) {
            ConfigurationSection s = tasks.getConfigurationSection(id);
            if (s == null) continue;
            out.add(new ScheduledTaskConfig(
                    id,
                    s.getString("mode", "sumo"),
                    s.getStringList("times"),
                    s.getStringList("days"),
                    s.getInt("min-players", defaultMinPlayers),
                    s.getInt("announce-before", 300),
                    s.getInt("auto-start-delay", 120),
                    s.getInt("map-radius", autoMapRadius),
                    s.getBoolean("generate-map", autoGenerateMap),
                    s.getString("arena-id", "")
            ));
        }
        return out;
    }

    public record ScheduledTaskConfig(
            String id,
            String modeId,
            List<String> times,
            List<String> days,
            int minPlayers,
            int announceBeforeSec,
            int autoStartDelaySec,
            int mapRadius,
            boolean generateMap,
            String arenaId
    ) {}
}
