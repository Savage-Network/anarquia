package com.minecraft.lobbyhost.account.context.objects.rank;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RankInfo {

    private Rank rank = Rank.builder().type(RankType.MEMBER).build();
    private List<Rank> availableRanks = new ArrayList<>();
}
