package com.minecraft.lobby.command;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.lobby.manager.MissionManager;
import com.minecraft.lobby.menu.mission.MissionMenu;
import org.bukkit.entity.Player;

public class MissionCommand implements CommandInheritor {
    
    @Command(name = "missoes", aliases = {"missao", "missions", "mission"})
    public void missoes(BukkitCommandContext context) {
        Player player = context.getPlayer();
        BedMember member = Core.getBedWarsData().of(player.getUniqueId());
        
        if (member == null) {
            player.sendMessage("§cErro ao carregar seus dados.");
            return;
        }
        
        String[] args = context.getArgs();
        
        if (args.length > 0 && args[0].equalsIgnoreCase("reset") && player.hasPermission("bedwars.admin")) {
            member.getMissionProgress().clear();
            member.saveMetadata(member.getMetadata());
            MissionManager.assignMissions(member);
            player.sendMessage("§aMissões resetadas e reatribuídas!");
            return;
        }
        
        new MissionMenu(player).handle();
    }
}
