package com.minecraft.game.api.vipkey;

import com.minecraft.game.account.context.objects.rank.RankType;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class VipKey {

    private final UUID id;
    private final String code;
    private final RankType rankType;
    private final long durationMs;
    private final UUID createdBy;
    private final String createdByName;
    private final long createdAt;
    private final UUID redeemedBy;
    private final String redeemedByName;
    private final long redeemedAt;

    public VipKey(UUID id, String code, RankType rankType, long durationMs,
                  UUID createdBy, String createdByName, long createdAt,
                  UUID redeemedBy, String redeemedByName, long redeemedAt) {
        this.id = id;
        this.code = code;
        this.rankType = rankType;
        this.durationMs = durationMs;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.redeemedBy = redeemedBy;
        this.redeemedByName = redeemedByName;
        this.redeemedAt = redeemedAt;
    }

    public boolean isRedeemed() {
        return redeemedAt > 0;
    }
}
