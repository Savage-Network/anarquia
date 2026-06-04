package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.event.PlayerBedBreakEvent;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.ranked.RankedPoints;
import com.minecraft.core.member.list.bedwars.objects.ranked.RankedTier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class RankedListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        User victimUser = (User) User.of(victim.getUniqueId());
        if (victimUser == null || !victimUser.isPlayer()) return;
        
        Arena arena = victimUser.getArena();
        if (arena == null || !arena.isPhase(RoomPhase.PLAYING)) return;

        BedMember victimMember = victimUser.getMember();
        
        // Aplicar pontos de morte
        victimMember.applyRankedPoints(RankedPoints.DEATH);
        
        // Notificar perda de pontos
        victim.sendMessage("§c" + RankedPoints.DEATH.getPoints() + " pontos de ranked §7(" + 
                          victimMember.getRankedPoints() + " total)");
        
        // Verificar se é final kill
        boolean isFinalKill = arena.isFinalKill(victimUser);
        
        if (killer != null) {
            User killerUser = (User) User.of(killer.getUniqueId());
            if (killerUser != null && killerUser.isPlayer()) {
                BedMember killerMember = killerUser.getMember();
                
                RankedTier oldTier = killerMember.getRankedTier();
                
                if (isFinalKill) {
                    // Aplicar pontos de final kill
                    killerMember.applyRankedPoints(RankedPoints.FINAL_KILL);
                    
                    // Notificar ganho de pontos
                    killer.sendMessage("§a+" + RankedPoints.FINAL_KILL.getPoints() + " pontos de ranked (Final Kill) §7(" + 
                                      killerMember.getRankedPoints() + " total)");
                } else {
                    // Aplicar pontos de kill normal
                    killerMember.applyRankedPoints(RankedPoints.KILL);
                    
                    // Notificar ganho de pontos
                    killer.sendMessage("§a+" + RankedPoints.KILL.getPoints() + " pontos de ranked §7(" + 
                                      killerMember.getRankedPoints() + " total)");
                }
                
                RankedTier newTier = killerMember.getRankedTier();
                
                // Notificar se subiu de rank
                if (oldTier != newTier) {
                    killer.sendMessage("");
                    killer.sendMessage("§6§lRANKED §e§l▸ §fVocê subiu para " + newTier.getColoredName() + "§f!");
                    killer.sendMessage("");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedBreak(PlayerBedBreakEvent event) {
        Arena arena = event.getArena();
        
        if (!arena.isPhase(RoomPhase.PLAYING)) return;

        // Aplicar pontos negativos para o time que perdeu a cama
        event.getBedTeam().getPlayers().forEach(player -> {
            User user = (User) User.of(player.getUniqueId());
            if (user != null && user.isPlayer()) {
                BedMember member = user.getMember();
                
                // Aplicar pontos de cama perdida
                member.applyRankedPoints(RankedPoints.BED_BREAK);
                
                // Notificar perda de pontos
                player.sendMessage("§c" + RankedPoints.BED_BREAK.getPoints() + " pontos de ranked (Cama perdida) §7(" + 
                                  member.getRankedPoints() + " total)");
            }
        });
    }
}
