package com.minecraft.core.member.list.bedwars.objects.mission;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MissionRegistry {
    
    private static final List<Mission> MISSIONS = new ArrayList<>();
    private static final Random RANDOM = new Random();
    
    static {
        // Missões Diárias
        MISSIONS.add(new Mission("0351", "Jogador presente", "Jogue 10 partidas", MissionType.PLAYER_PRESENT, MissionPeriod.DAILY, 10, 1250, Material.INK_SACK));
        MISSIONS.add(new Mission("0339", "Ninguém me para!", "Elimine 2 adversários seguidos em uma partida", MissionType.DAILY, MissionPeriod.DAILY, 2, 1250, Material.INK_SACK));
        MISSIONS.add(new Mission("0340", "Destruidor", "Destrua 5 camas", MissionType.DAILY, MissionPeriod.DAILY, 5, 1500, Material.INK_SACK));
        MISSIONS.add(new Mission("0341", "Assassino", "Consiga 20 kills", MissionType.DAILY, MissionPeriod.DAILY, 20, 1000, Material.INK_SACK));
        MISSIONS.add(new Mission("0342", "Vitorioso", "Vença 3 partidas", MissionType.DAILY, MissionPeriod.DAILY, 3, 2000, Material.INK_SACK));
        
        // Missões Semanais
        MISSIONS.add(new Mission("0350", "ZUUUUUMMM", "Utilize 50 Bolas de Fogo", MissionType.WEEKLY, MissionPeriod.WEEKLY, 50, 5000, Material.INK_SACK));
        MISSIONS.add(new Mission("0343", "Veloz e furioso", "Compre 5 poções de agilidade", MissionType.WEEKLY, MissionPeriod.WEEKLY, 5, 5000, Material.INK_SACK));
        MISSIONS.add(new Mission("0352", "Mestre das camas", "Destrua 20 camas", MissionType.WEEKLY, MissionPeriod.WEEKLY, 20, 7500, Material.INK_SACK));
        MISSIONS.add(new Mission("0353", "Exterminador", "Consiga 100 kills", MissionType.WEEKLY, MissionPeriod.WEEKLY, 100, 10000, Material.INK_SACK));
        MISSIONS.add(new Mission("0354", "Campeão", "Vença 15 partidas", MissionType.WEEKLY, MissionPeriod.WEEKLY, 15, 15000, Material.INK_SACK));
    }
    
    public static List<Mission> getAllMissions() {
        return new ArrayList<>(MISSIONS);
    }
    
    public static Mission getMission(String id) {
        return MISSIONS.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public static List<Mission> getDailyMissions() {
        return MISSIONS.stream()
                .filter(m -> m.getPeriod() == MissionPeriod.DAILY)
                .collect(Collectors.toList());
    }
    
    public static List<Mission> getWeeklyMissions() {
        return MISSIONS.stream()
                .filter(m -> m.getPeriod() == MissionPeriod.WEEKLY)
                .collect(Collectors.toList());
    }
    
    public static List<Mission> getRandomDailyMissions(int count) {
        List<Mission> daily = getDailyMissions();
        List<Mission> selected = new ArrayList<>();
        
        while (selected.size() < count && !daily.isEmpty()) {
            Mission mission = daily.remove(RANDOM.nextInt(daily.size()));
            selected.add(mission);
        }
        
        return selected;
    }
    
    public static List<Mission> getRandomWeeklyMissions(int count) {
        List<Mission> weekly = getWeeklyMissions();
        List<Mission> selected = new ArrayList<>();
        
        while (selected.size() < count && !weekly.isEmpty()) {
            Mission mission = weekly.remove(RANDOM.nextInt(weekly.size()));
            selected.add(mission);
        }
        
        return selected;
    }
}
