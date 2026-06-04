package com.minecraft.core.member.list.bedwars.objects.mission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MissionProgress {
    
    private String missionId;
    private int progress;
    private boolean completed;
    private long expiresAt;
    
    public MissionProgress(String missionId, long expiresAt) {
        this.missionId = missionId;
        this.progress = 0;
        this.completed = false;
        this.expiresAt = expiresAt;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    public void addProgress(int amount) {
        this.progress += amount;
    }
}
