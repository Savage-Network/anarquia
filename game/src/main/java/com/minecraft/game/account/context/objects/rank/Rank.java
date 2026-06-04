package com.minecraft.game.account.context.objects.rank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rank {

    private RankType type;
    private long givenAt;
    private long expiresAt;

    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }
}
