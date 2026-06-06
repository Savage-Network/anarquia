package com.minecraft.game.punishment;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.api.punishment.PunishmentCategory;
import com.minecraft.game.api.punishment.PunishmentReason;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Regras configuráveis de punição ({@code plugins/game/punishments.yml}). */
public final class PunishmentRules {

    public record Rule(
            String id,
            PunishmentCategory category,
            PunishmentReason reason,
            String label,
            Material icon,
            String color,
            List<String> steps
    ) {
        public String resolveTime(int offenseIndex) {
            if (steps == null || steps.isEmpty()) return "-1";
            int idx = Math.max(0, Math.min(offenseIndex, steps.size() - 1));
            return steps.get(idx);
        }

        public String coloredLabel() {
            return color + label;
        }
    }

    private static final Map<String, Rule> RULES = new LinkedHashMap<>();
    private static List<String> customTimes = List.of();
    private static List<String> kickReasons = List.of();
    private static boolean silentDefault;

    private PunishmentRules() {}

    public static void load(GamePlugin plugin) {
        File file = new File(plugin.getDataFolder(), "punishments.yml");
        if (!file.exists()) {
            plugin.saveResource("punishments.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        mergeDefaults(plugin, yaml, file);

        RULES.clear();
        ConfigurationSection rulesSec = yaml.getConfigurationSection("rules");
        if (rulesSec != null) {
            for (String id : rulesSec.getKeys(false)) {
                ConfigurationSection sec = rulesSec.getConfigurationSection(id);
                if (sec == null) continue;
                PunishmentCategory cat = PunishmentCategory.of(sec.getString("category"));
                PunishmentReason reason = PunishmentReason.of(sec.getString("reason"));
                if (cat == null || reason == null) continue;

                Material mat = Material.matchMaterial(sec.getString("icon", "PAPER"));
                if (mat == null) mat = Material.PAPER;

                List<String> steps = sec.getStringList("steps");
                if (steps.isEmpty()) steps = List.of("-1");

                RULES.put(id, new Rule(
                        id,
                        cat,
                        reason,
                        sec.getString("label", id),
                        mat,
                        sec.getString("color", "&7"),
                        List.copyOf(steps)
                ));
            }
        }

        customTimes = yaml.getStringList("custom-times");
        if (customTimes.isEmpty()) {
            customTimes = List.of("30m", "1h", "6h", "1d", "7d", "-1");
        }
        kickReasons = yaml.getStringList("kick-reasons");
        if (kickReasons.isEmpty()) {
            kickReasons = List.of("Comportamento inadequado", "Outro motivo");
        }
        silentDefault = yaml.getBoolean("settings.silent-default", false);
    }

    private static void mergeDefaults(GamePlugin plugin, FileConfiguration yaml, File file) {
        try (InputStream in = plugin.getResource("punishments.yml")) {
            if (in == null) return;
            yaml.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8)));
            yaml.options().copyDefaults(true);
            yaml.save(file);
        } catch (Exception ignored) {
        }
    }

    public static Rule get(String id) {
        return RULES.get(id);
    }

    public static List<Rule> all() {
        return List.copyOf(RULES.values());
    }

    public static List<Rule> forCategory(PunishmentCategory category) {
        List<Rule> out = new ArrayList<>();
        for (Rule rule : RULES.values()) {
            if (rule.category() == category) out.add(rule);
        }
        return out;
    }

    public static List<String> customTimes() {
        return Collections.unmodifiableList(customTimes);
    }

    public static List<String> kickReasons() {
        return Collections.unmodifiableList(kickReasons);
    }

    public static boolean silentDefault() {
        return silentDefault;
    }

    public static String formatTime(String time) {
        if (time == null || time.equals("-1") || time.equalsIgnoreCase("perm")
                || time.equalsIgnoreCase("perma")) {
            return "Permanente";
        }
        return time;
    }

    /** Índice 0-based para o próximo passo de escalonamento. */
    public static int offenseIndex(int priorCount) {
        return Math.max(0, priorCount);
    }
}
