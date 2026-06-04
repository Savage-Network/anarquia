package com.minecraft.arcade.lobby.manager;

import com.minecraft.arcade.lobby.listener.DiscordHologramListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class DiscordHologramManager {
    
    private static DiscordHologramListener hologramListener;
    private static boolean initialized = false;
    private static Location customLocation = null;
    
    public static void initialize(Plugin plugin) {
        if (initialized) return;
        
        // Registrar o listener
        hologramListener = new DiscordHologramListener();
        Bukkit.getPluginManager().registerEvents(hologramListener, plugin);
        
        // Não criar holograma automaticamente, esperar definição de localização
        initialized = true;
        System.out.println("[DiscordHologram] Sistema de holograma do Discord inicializado no Lobby!");
        System.out.println("[DiscordHologram] Use /setdiscord para definir a localização do holograma!");
    }
    
    public static void setLocation(Location location) {
        customLocation = location.clone();
        System.out.println("[DiscordHologram] Localização definida: " + formatLocation(location));
        
        // Se já estiver inicializado, recriar o holograma na nova localização
        if (hologramListener != null) {
            hologramListener.removeHologram();
            hologramListener.createDiscordHologram();
            respawnForAllPlayers();
        }
    }
    
    public static Location getLocation() {
        return customLocation;
    }
    
    public static boolean hasLocation() {
        return customLocation != null;
    }
    
    public static void respawnForAllPlayers() {
        if (hologramListener != null && Bukkit.getOnlinePlayers() != null && hasLocation()) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                hologramListener.respawnHologram(player);
            });
        }
    }
    
    public static void shutdown() {
        if (hologramListener != null) {
            hologramListener.removeHologram();
        }
        initialized = false;
        customLocation = null;
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    private static String formatLocation(Location loc) {
        return String.format("%.3f, %.3f, %.3f", loc.getX(), loc.getY(), loc.getZ());
    }
}
