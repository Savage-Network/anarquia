package com.minecraft.arcade.lobby.command;

import com.minecraft.arcade.lobby.manager.DiscordHologramManager;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class SetDiscordCommand implements CommandInheritor {

    @Command(name = "setdiscord", rank = RankType.ADMIN)
    public void setdiscord(BukkitCommandContext context) {
        if (!(context.getSender() instanceof Player)) {
            return;
        }

        Player player = context.getPlayer();
        
        // Definir localização do holograma para onde o jogador está
        DiscordHologramManager.setLocation(player.getLocation());
        
        player.sendMessage("§aHolograma do Discord definido na sua localização!");
        player.sendMessage("§7Coordenadas: " + formatLocation(player.getLocation()));
        player.sendMessage("§7Mundo: " + player.getWorld().getName());
        player.sendMessage("§7Use /discordhologram para respawnar o holograma se necessário.");
    }
    
    private String formatLocation(org.bukkit.Location loc) {
        return String.format("%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ());
    }
}
