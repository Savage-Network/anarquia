package com.minecraft.game.leaderboard;

import org.bukkit.Material;

public enum LeaderboardType {

    KILLS("§c§lKills", Material.DIAMOND_SWORD),
    DEATHS("§7§lMortes", Material.SKELETON_SKULL),
    GOLDS("§a§lDinheiro", Material.GOLD_INGOT),
    KDR("§e§lK/D", Material.NETHER_STAR),
    CLANS("§3§lClãs", Material.SHIELD);

    private final String title;
    private final Material icon;

    LeaderboardType(String title, Material icon) {
        this.title = title;
        this.icon = icon;
    }

    public String title() { return title; }
    public Material icon() { return icon; }

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
}
