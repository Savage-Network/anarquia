package com.minecraft.core.account.context.objects.code;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@AllArgsConstructor
public class RedeemCode {

    private String code;
    private RankType rankType;
    private long duration; // em milissegundos
    private UUID creator;
    private long createdAt;
    private boolean used;
    private UUID usedBy;
    private long usedAt;

    public RedeemCode(String code, RankType rankType, long duration, UUID creator) {
        this.code = code;
        this.rankType = rankType;
        this.duration = duration;
        this.creator = creator;
        this.createdAt = System.currentTimeMillis();
        this.used = false;
        this.usedBy = null;
        this.usedAt = 0;
    }

    public void markAsUsed(UUID player) {
        this.used = true;
        this.usedBy = player;
        this.usedAt = System.currentTimeMillis();
    }

    public String getFormattedDuration() {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        if (days > 0) {
            return days + " dia" + (days > 1 ? "s" : "");
        }
        
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "");
        }
        
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        return minutes + " minuto" + (minutes > 1 ? "s" : "");
    }
}
