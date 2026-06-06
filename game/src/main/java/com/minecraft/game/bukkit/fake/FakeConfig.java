package com.minecraft.game.bukkit.fake;

import com.minecraft.game.GamePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class FakeConfig {

    public enum RandomMode {
        LIST,
        GENERATE,
        MIXED;

        static RandomMode fromString(String raw) {
            if (raw == null) return LIST;
            return switch (raw.toLowerCase(Locale.ROOT)) {
                case "generate", "gerar" -> GENERATE;
                case "mixed", "misto" -> MIXED;
                default -> LIST;
            };
        }
    }

    private static boolean enabled = true;
    private static boolean requireVip = true;
    private static boolean hideRank = true;
    private static boolean hideMedal = true;
    private static boolean hideClanTag = true;
    private static boolean hideCosmeticTitle = true;
    private static boolean applySkin = true;
    private static boolean changeProfileName = true;
    private static long cooldownMs = 60_000L;
    private static List<String> blocked = new ArrayList<>();
    private static RandomMode randomMode = RandomMode.LIST;
    private static List<String> randomNames = new ArrayList<>();
    private static List<String> randomPrefixes = new ArrayList<>();
    private static List<String> randomSuffixes = new ArrayList<>();

    private FakeConfig() {}

    public static void load(GamePlugin plugin) {
        File file = new File(plugin.getDataFolder(), "fake.yml");
        if (!file.exists()) {
            plugin.saveResource("fake.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        enabled = yaml.getBoolean("enabled", true);
        requireVip = yaml.getBoolean("require-vip", true);
        hideRank = yaml.getBoolean("hide-rank", true);
        hideMedal = yaml.getBoolean("hide-medal", true);
        hideClanTag = yaml.getBoolean("hide-clan-tag", true);
        hideCosmeticTitle = yaml.getBoolean("hide-cosmetic-title", true);
        applySkin = yaml.getBoolean("apply-skin", true);
        changeProfileName = yaml.getBoolean("change-profile-name", true);
        cooldownMs = yaml.getLong("cooldown-ms", 60_000L);
        blocked = yaml.getStringList("blocked-names");
        randomMode = RandomMode.fromString(yaml.getString("random.mode", "list"));
        randomNames = parseRandomNames(yaml.getStringList("random.names"));
        randomPrefixes = yaml.getStringList("random.prefixes");
        randomSuffixes = yaml.getStringList("random.suffixes");
        if (randomPrefixes.isEmpty()) {
            randomPrefixes = List.of("Dark", "Silent", "Real", "Pro", "Mr", "xX", "Not", "Sky", "Red", "Blue");
        }
        if (randomSuffixes.isEmpty()) {
            randomSuffixes = List.of("Gamer", "Player", "PvP", "Craft", "BR", "MC", "King", "Fox", "Wolf", "Star");
        }
    }

    private static List<String> parseRandomNames(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        List<String> parsed = new ArrayList<>();
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) continue;
            String nick = entry.trim();
            if (nick.length() < 3 || nick.length() > 16) continue;
            if (!nick.matches("^[a-zA-Z0-9_]+$")) continue;
            if (isBlocked(nick)) continue;
            parsed.add(nick);
        }
        return Collections.unmodifiableList(parsed);
    }

    public static boolean isEnabled() { return enabled; }
    public static boolean isRequireVip() { return requireVip; }
    public static boolean isHideRank() { return hideRank; }
    public static boolean isHideMedal() { return hideMedal; }
    public static boolean isHideClanTag() { return hideClanTag; }
    public static boolean isHideCosmeticTitle() { return hideCosmeticTitle; }
    public static boolean isApplySkin() { return applySkin; }
    public static boolean isChangeProfileName() { return changeProfileName; }
    public static long getCooldownMs() { return cooldownMs; }
    public static List<String> getBlocked() { return Collections.unmodifiableList(blocked); }
    public static RandomMode getRandomMode() { return randomMode; }
    public static List<String> getRandomNames() { return randomNames; }
    public static List<String> getRandomPrefixes() { return randomPrefixes; }
    public static List<String> getRandomSuffixes() { return randomSuffixes; }

    public static boolean isBlocked(String nick) {
        if (nick == null) return true;
        String lower = nick.toLowerCase(Locale.ROOT);
        for (String b : blocked) {
            if (b != null && b.equalsIgnoreCase(lower)) return true;
        }
        return false;
    }
}
