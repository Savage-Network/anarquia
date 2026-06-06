package com.minecraft.game.leaderboard;

import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Locale;

public enum LeaderboardType {

    KILLS("§c§lKills", Material.DIAMOND_SWORD),
    DEATHS("§7§lMortes", Material.SKELETON_SKULL),
    GOLDS("§a§lDinheiro", Material.GOLD_INGOT),
    KDR("§e§lK/D", Material.NETHER_STAR),
    CLANS("§3§lClãs", Material.SHIELD);

    private final String legacyTitle;
    private final Material icon;

    LeaderboardType(String legacyTitle, Material icon) {
        this.legacyTitle = legacyTitle;
        this.icon = icon;
    }

    public String title() {
        return legacyTitle;
    }

    public String title(Player player) {
        return MenuTexts.legacy(player, "menus.leaderboard.type." + name().toLowerCase());
    }

    public Material icon() {
        return icon;
    }

    public static LeaderboardType fromArg(String raw) {
        if (raw == null) return null;
        return switch (raw.toLowerCase()) {
            case "deaths", "mortes" -> DEATHS;
            case "money", "golds", "saldo", "dinheiro" -> GOLDS;
            case "kdr", "kd" -> KDR;
            case "clans", "clan", "cla" -> CLANS;
            case "kills", "kill" -> KILLS;
            default -> null;
        };
    }

    /** Id usado em {@code /holo set} e {@code leaderboard-holograms.yml}. */
    public String holoId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
