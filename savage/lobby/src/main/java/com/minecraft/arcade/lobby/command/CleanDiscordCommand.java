package com.minecraft.arcade.lobby.command;

import com.minecraft.arcade.lobby.manager.DiscordHologramManager;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class CleanDiscordCommand implements CommandInheritor {

    @Command(name = "cleandiscord", rank = RankType.ADMIN)
    public void cleandiscord(BukkitCommandContext context) {
        if (!(context.getSender() instanceof Player)) {
            return;
        }

        Player player = context.getPlayer();
        
        if (!DiscordHologramManager.isInitialized()) {
            player.sendMessage("§cO sistema de holograma do Discord não está inicializado!");
            return;
        }

        if (!DiscordHologramManager.hasLocation()) {
            player.sendMessage("§cNenhuma localização definida para o holograma do Discord!");
            player.sendMessage("§7Use /setdiscord para definir a localização.");
            return;
        }

        // Forçar limpeza completa e recriação
        DiscordHologramManager.getLocation(); // Verificar localização
        
        // Recriar holograma completamente
        player.sendMessage("§aLimpando holograma do Discord...");
        
        // Forçar respawn para todos os jogadores online
        DiscordHologramManager.respawnForAllPlayers();
        
        player.sendMessage("§aHolograma do Discord limpo e recriado sem itens!");
        player.sendMessage("§7Apenas o texto '§b§lDISCORD:§7' será exibido.");
    }
}
