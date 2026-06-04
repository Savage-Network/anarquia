package com.minecraft.core.member.context;

import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.member.context.stats.ArcadeStats;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class MemberContext {

    private final List<MenuMetadata> menus = new ArrayList<>();
    private final Map<ArcadeCategory, ArcadeStats> stats = new ConcurrentHashMap<>();

    private int level, levelXp, coins;

    private int weekLevelXp, monthLevelXp;

    private final long joinedAt = System.currentTimeMillis();
}
