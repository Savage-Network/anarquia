package com.minecraft.lobby.manager;

import com.minecraft.core.Core;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.mission.Mission;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionPeriod;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionProgress;
import com.minecraft.core.member.list.bedwars.objects.mission.MissionRegistry;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MissionManager {
    
    private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final long WEEK_IN_MILLIS = TimeUnit.DAYS.toMillis(7);
    
    public static void assignMissions(BedMember member) {
        member.removeExpiredMissions();
        
        // Verificar se precisa de missões diárias
        long dailyCount = member.getMissionProgress().stream()
                .filter(p -> !p.isExpired())
                .map(p -> MissionRegistry.getMission(p.getMissionId()))
                .filter(m -> m != null && m.getPeriod() == MissionPeriod.DAILY)
                .count();
        
        if (dailyCount < 3) {
            List<Mission> dailyMissions = MissionRegistry.getRandomDailyMissions(3 - (int) dailyCount);
            long expiresAt = System.currentTimeMillis() + DAY_IN_MILLIS;
            
            for (Mission mission : dailyMissions) {
                if (member.getProgress(mission.getId()) == null) {
                    member.addMission(new MissionProgress(mission.getId(), expiresAt));
                }
            }
        }
        
        // Verificar se precisa de missões semanais
        long weeklyCount = member.getMissionProgress().stream()
                .filter(p -> !p.isExpired())
                .map(p -> MissionRegistry.getMission(p.getMissionId()))
                .filter(m -> m != null && m.getPeriod() == MissionPeriod.WEEKLY)
                .count();
        
        if (weeklyCount < 2) {
            List<Mission> weeklyMissions = MissionRegistry.getRandomWeeklyMissions(2 - (int) weeklyCount);
            long expiresAt = System.currentTimeMillis() + WEEK_IN_MILLIS;
            
            for (Mission mission : weeklyMissions) {
                if (member.getProgress(mission.getId()) == null) {
                    member.addMission(new MissionProgress(mission.getId(), expiresAt));
                }
            }
        }
    }
}
