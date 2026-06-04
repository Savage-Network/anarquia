package com.minecraft.core.account.context.objects.rank.info;

import com.minecraft.core.account.context.objects.rank.Rank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class RankInfo {

    private Rank rank;

    private List<Rank> availableRanks = new ArrayList<>();
}
