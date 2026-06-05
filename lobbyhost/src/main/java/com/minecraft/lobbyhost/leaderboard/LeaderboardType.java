package com.minecraft.lobbyhost.leaderboard;

import org.bukkit.Material;

public enum LeaderboardType {

    KILLS("§c§lKills", Material.DIAMOND_SWORD),
    DEATHS("§7§lMortes", Material.SKELETON_SKULL),
    GOLDS("§a§lMoney (game)", Material.GOLD_INGOT),
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
}
