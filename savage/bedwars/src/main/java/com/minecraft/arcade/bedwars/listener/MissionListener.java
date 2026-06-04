package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.event.PlayerBedBreakEvent;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.mission.Mission;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionProgress;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MissionListener implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) return;
        
        // Verificar se está em uma arena
        User killerUser = (User) User.of(killer.getUniqueId());
        if (killerUser == null) return;
        
        Arena arena = killerUser.getArena();
        if (arena == null || !arena.isPhase(RoomPhase.PLAYING)) return;
        
        BedMember member = Core.getBedWarsData().of(killer.getUniqueId());
        if (member == null) return;
        
        // Missão: Elimine adversários
        updateMissionProgress(member, "0339", 1); // Ninguém me para! (2 kills seguidos)
        updateMissionProgress(member, "0341", 1); // Assassino (20 kills)
        updateMissionProgress(member, "0353", 1); // Exterminador (100 kills)
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedBreak(PlayerBedBreakEvent event) {
        Player player = event.getPlayer();
        
        BedMember member = Core.getBedWarsData().of(player.getUniqueId());
        if (member == null) return;
        
        // Missão: Destrua camas
        updateMissionProgress(member, "0340", 1); // Destruidor (5 camas)
        updateMissionProgress(member, "0352", 1); // Mestre das camas (20 camas)
    }
    
    private void updateMissionProgress(BedMember member, String missionId, int amount) {
        MissionProgress progress = member.getProgress(missionId);
        
        if (progress == null || progress.isCompleted() || progress.isExpired()) {
            return;
        }
        
        Mission mission = MissionRegistry.getMission(missionId);
        
        if (mission != null) {
            int oldProgress = progress.getProgress();
            member.addMissionProgress(missionId, amount);
            int newProgress = progress.getProgress();
            
            // Verificar se completou (usar newProgress atualizado)
            if (newProgress >= mission.getTarget() && oldProgress < mission.getTarget()) {
                Player player = member.getAccount().player();
                if (player != null) {
                    player.sendMessage("§a§lMISSÃO COMPLETA! §f" + mission.getName());
                    player.sendMessage("§7Vá até o NPC de missões para coletar sua recompensa!");
                }
            }
        }
    }
}
