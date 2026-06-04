package com.minecraft.arcade.lobby.command;

import com.minecraft.arcade.lobby.manager.DiscordHologramManager;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class DiscordHologramCommand implements CommandInheritor {

    @Command(name = "discordhologram", aliases = {"dh", "discordholo"}, rank = RankType.ADMIN)
    public void discordhologram(BukkitCommandContext context) {
        if (!(context.getSender() instanceof Player)) {
            return; // Se não for jogador, apenas retorna sem mensagem
        }

        Player player = context.getPlayer();
        
        if (!DiscordHologramManager.isInitialized()) {
            player.sendMessage("§cO sistema de holograma do Discord não está inicializado!");
            return;
        }

        // Verificar se existe localização definida
        if (!DiscordHologramManager.hasLocation()) {
            player.sendMessage("§cNenhuma localização definida para o holograma do Discord!");
            player.sendMessage("§7Use /setdiscord para definir a localização onde você está parado.");
            return;
        }

        // Respawnar holograma para todos os jogadores
        DiscordHologramManager.respawnForAllPlayers();
        
        player.sendMessage("§aHolograma do Discord respawnado para todos os jogadores!");
        
        // Mostrar informações da localização
        org.bukkit.Location loc = DiscordHologramManager.getLocation();
        player.sendMessage("§7Coordenadas: " + formatLocation(loc));
        player.sendMessage("§7Mundo: " + loc.getWorld().getName());
        player.sendMessage("§7Link: https://discord.gg/76s8agkgW6");
        player.sendMessage("§7Use /setdiscord para mudar a localização.");
    }
    
    private String formatLocation(org.bukkit.Location loc) {
        return String.format("%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ());
    }
}
