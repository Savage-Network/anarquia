package com.minecraft.proxyhost.whitelist;

import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public final class WhitelistRule {

    private boolean enabled;
    private RankType minRank = RankType.HELPER;
    private final Set<RankType> extraRanks = new LinkedHashSet<>();
    /** UUID → último nick conhecido (para exibição). */
    private final Map<UUID, String> bypassPlayers = new LinkedHashMap<>();

    public WhitelistRule copy() {
        WhitelistRule copy = new WhitelistRule();
        copy.enabled = enabled;
        copy.minRank = minRank;
        copy.extraRanks.addAll(extraRanks);
        copy.bypassPlayers.putAll(bypassPlayers);
        return copy;
    }
}
