package com.minecraft.arcade.lobby.listener;

import com.minecraft.arcade.lobby.manager.DiscordHologramManager;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Arrays;
import java.util.UUID;

public class DiscordHologramListener implements Listener {

    private HologramServer discordHologram;

    public DiscordHologramListener() {
        // Não criar holograma automaticamente no construtor
    }

    public void createDiscordHologram() {
        try {
            Location discordLocation = DiscordHologramManager.getLocation();
            
            if (discordLocation == null) {
                System.err.println("[DiscordHologram] Nenhuma localização definida para o holograma do Discord!");
                return;
            }

            // Procurar pelo mundo do lobby se não estiver definido
            if (discordLocation.getWorld() == null) {
                org.bukkit.World world = org.bukkit.Bukkit.getWorld("lobby");
                
                if (world == null) {
                    // Tentar outros nomes comuns de lobby
                    world = org.bukkit.Bukkit.getWorld("world");
                    if (world == null) {
                        world = org.bukkit.Bukkit.getWorld("spawn");
                        if (world == null) {
                            world = org.bukkit.Bukkit.getWorlds().isEmpty() ? null : org.bukkit.Bukkit.getWorlds().get(0);
                        }
                    }
                }
                
                if (world == null) {
                    System.err.println("[DiscordHologram] Nenhum mundo de lobby encontrado para criar o holograma!");
                    return;
                }
                
                discordLocation.setWorld(world);
            }
            
            System.out.println("[DiscordHologram] Holograma do Discord será criado no mundo: " + discordLocation.getWorld().getName());
            
            // Criar holograma na coordenada específica
            Location hologramLoc = discordLocation.clone().add(0.5, 1.5, 0.5);

            discordHologram = BukkitCore.getManager().getHologram().spawnServer(
                    "discord-" + UUID.randomUUID().toString().substring(0, 8),
                    hologramLoc
            );

            // Definir texto azul claro e em negrito (apenas texto, sem itens)
            discordHologram.setText(Arrays.asList("§b§lDISCORD:"));
            
            // Garantir que o holograma seja apenas texto sem itens
            discordHologram.getRows().forEach(row -> {
                row.small(true); // Tornar o armor stand pequeno
            });

            // Spawnar para todos os jogadores online no mundo
            discordLocation.getWorld().getPlayers().forEach(player -> {
                discordHologram.spawnTo(player);
            });

            System.out.println("[DiscordHologram] Holograma do Discord criado em: " + formatLocation(discordLocation) + " no mundo: " + discordLocation.getWorld().getName());
        } catch (Exception e) {
            System.err.println("[DiscordHologram] Erro ao criar holograma do Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String formatLocation(Location loc) {
        return String.format("%.3f, %.3f, %.3f", loc.getX(), loc.getY(), loc.getZ());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDiscordHologramClick(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;

        Player player = event.getPlayer();
        Location clickedLocation = event.getClickedBlock().getLocation();

        // Verificar se o jogador clicou perto do holograma
        if (isNearDiscordLocation(clickedLocation, 5.0)) {
            event.setCancelled(true);
            
            // Cancelar completamente a interação para não dar itens
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);

            // Enviar mensagem com o link do Discord
            player.sendMessage("");
            player.sendMessage("§b§lDISCORD:");
            player.sendMessage("§fhttps://discord.gg/76s8agkgW6");
            player.sendMessage("");
            player.sendMessage("§7Clique no link acima para entrar no nosso Discord!");
            player.sendMessage("");

            // Tocar som de clique
            player.playSound(player.getLocation(), org.bukkit.Sound.CLICK, 1.0f, 1.0f);

            // Mostrar mensagem no console para debug
            System.out.println("[DiscordHologram] " + player.getName() + " clicou no holograma do Discord");
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Spawnar holograma para o jogador se ele entrar no lobby e se existir localização definida
        Core.getPlatform().runSync(() -> {
            if (isInLobby(player.getWorld()) && discordHologram != null && DiscordHologramManager.hasLocation()) {
                discordHologram.spawnTo(player);
                System.out.println("[DiscordHologram] Holograma spawnado para: " + player.getName());
            }
        }, 20L); // 1 segundo depois de entrar
    }
    
    private boolean isInLobby(org.bukkit.World world) {
        return world != null && (
            world.getName().equalsIgnoreCase("lobby") ||
            world.getName().equalsIgnoreCase("world") ||
            world.getName().equalsIgnoreCase("spawn")
        );
    }

    private boolean isNearDiscordLocation(Location location, double radius) {
        Location discordLocation = DiscordHologramManager.getLocation();
        
        if (discordLocation == null || location.getWorld() == null || discordLocation.getWorld() == null) {
            return false;
        }

        if (!location.getWorld().equals(discordLocation.getWorld())) {
            return false;
        }

        double distance = location.distance(discordLocation);
        return distance <= radius;
    }

    public void respawnHologram(Player player) {
        if (discordHologram != null) {
            // Limpar completamente o holograma para o jogador
            discordHologram.despawnTo(player);
            
            // Re-spawnar sem itens
            discordHologram.spawnTo(player);
            
            // Garantir que seja apenas texto
            discordHologram.getRows().forEach(row -> {
                row.small(true);
            });
        }
    }

    public void removeHologram() {
        if (discordHologram != null) {
            // Remover para todos os viewers
            new java.util.ArrayList<>(discordHologram.getViewers()).forEach(discordHologram::despawnTo);
            BukkitCore.getManager().getHologram().getHolograms().remove(discordHologram.getId());
            discordHologram = null;
        }
    }
}
