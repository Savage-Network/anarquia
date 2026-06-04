package com.minecraft.lobby.menu.mission;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.mission.Mission;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionPeriod;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionProgress;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionRegistry;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MissionMenu extends Menu {
    
    private final BedMember member;
    
    public MissionMenu(Player player) {
        super(player, "Missões do Bed Wars", 4);
        this.member = Core.getBedWarsData().of(player.getUniqueId());
    }
    
    @Override
    public void handle() {
        member.removeExpiredMissions();
        
        // Coletar missões diárias
        List<MissionProgress> dailyMissions = new ArrayList<>();
        for (MissionProgress progress : member.getMissionProgress()) {
            if (!progress.isExpired()) {
                Mission mission = MissionRegistry.getMission(progress.getMissionId());
                if (mission != null && mission.getPeriod() == MissionPeriod.DAILY) {
                    dailyMissions.add(progress);
                }
            }
        }
        
        // Slot 10: Corante verde (indicador)
        addItem(10, Item.of(Material.INK_SACK, 10, "§aMissões Diárias", "§7" + dailyMissions.size() + " missões ativas"));
        
        // Slots 11-13: Missões diárias (papéis encantados)
        int dailySlot = 11;
        for (int i = 0; i < Math.min(dailyMissions.size(), 3); i++) {
            MissionProgress progress = dailyMissions.get(i);
            Mission mission = MissionRegistry.getMission(progress.getMissionId());
            if (mission != null) {
                addMissionItem(dailySlot++, mission, progress);
            }
        }
        
        // Coletar missões semanais
        List<MissionProgress> weeklyMissions = new ArrayList<>();
        for (MissionProgress progress : member.getMissionProgress()) {
            if (!progress.isExpired()) {
                Mission mission = MissionRegistry.getMission(progress.getMissionId());
                if (mission != null && mission.getPeriod() == MissionPeriod.WEEKLY) {
                    weeklyMissions.add(progress);
                }
            }
        }
        
        // Slot 19: Corante amarelo (indicador)
        addItem(19, Item.of(Material.INK_SACK, 11, "§eMissões Semanais", "§7" + weeklyMissions.size() + " missões ativas"));
        
        // Slots 20-24: Missões semanais (papéis encantados)
        int weeklySlot = 20;
        for (int i = 0; i < Math.min(weeklyMissions.size(), 5); i++) {
            MissionProgress progress = weeklyMissions.get(i);
            Mission mission = MissionRegistry.getMission(progress.getMissionId());
            if (mission != null) {
                addMissionItem(weeklySlot++, mission, progress);
            }
        }
        
        display();
    }
    
    private void addMissionItem(int slot, Mission mission, MissionProgress progress) {
        List<String> lore = new ArrayList<>();
        
        lore.add(mission.getFormattedPeriod());
        lore.add("");
        lore.add("§7" + mission.getDescription() + ": §f" + progress.getProgress() + "/" + mission.getTarget());
        lore.add("");
        
        long timeLeft = progress.getExpiresAt() - System.currentTimeMillis();
        String timeStr = formatTime(timeLeft);
        lore.add("§fTempo restante:");
        lore.add("§a" + timeStr);
        lore.add("");
        lore.add("§fRecompensa:");
        lore.add("§b+" + Util.formatNumber(mission.getXpReward()) + " XP §7(Bed Wars)");
        lore.add("");
        
        if (!progress.isCompleted()) {
            if (progress.getProgress() >= mission.getTarget()) {
                lore.add("§aCompleta!");
                addItem(slot, Item.of(Material.ENCHANTED_BOOK, mission.getFormattedName(), lore)
                        .interact(event -> {
                            int oldLevel = member.getLevel();
                            int oldXp = member.getLevelXp();
                            
                            member.completeMission(mission.getId());
                            member.addLevelXp(mission.getXpReward());
                            
                            int newLevel = member.getLevel();
                            int newXp = member.getLevelXp();
                            
                            getPlayer().sendMessage("§a§lMISSÃO COMPLETA!");
                            getPlayer().sendMessage("§7" + mission.getName());
                            getPlayer().sendMessage("");
                            getPlayer().sendMessage("§fRecompensa: §b+" + Util.formatNumber(mission.getXpReward()) + " XP");
                            
                            if (newLevel > oldLevel) {
                                getPlayer().sendMessage("");
                                getPlayer().sendMessage("§6§lPARABÉNS! §eVocê subiu para o nível " + newLevel + "!");
                            } else {
                                getPlayer().sendMessage("§7Progresso: §f" + newXp + "/" + member.getMinLevelXp() + " XP §7(Nível " + newLevel + ")");
                            }
                            
                            getPlayer().closeInventory();
                        }));
            } else {
                lore.add("§eEm progresso.");
                addItem(slot, Item.of(Material.PAPER, mission.getFormattedName(), lore));
            }
        } else {
            lore.add("§aCompleta!");
            addItem(slot, Item.of(Material.ENCHANTED_BOOK, mission.getFormattedName(), lore));
        }
    }
    
    private String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        
        if (days > 0) {
            return days + " dia" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "");
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
            return minutes + " minuto" + (minutes > 1 ? "s" : "");
        }
    }
}
