package com.minecraft.core.server.payload.whitelist;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ServerWhitelist {

    private boolean enabled = false;

    private final List<RankType> ranks = new ArrayList<>();
    private final List<String> players = new ArrayList<>();

    public boolean hasRank(RankType rank) {
        return ranks.contains(rank);
    }

    public boolean hasPlayer(String name) {
        return players.stream().anyMatch(search -> search.equalsIgnoreCase(name));
    }
}
