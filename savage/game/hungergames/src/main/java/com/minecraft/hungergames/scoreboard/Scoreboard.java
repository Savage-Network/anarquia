package com.minecraft.hungergames.scoreboard;

import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.hungergames.Hungergames;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public abstract class Scoreboard {

    protected final Hungergames plugin;

    public abstract Sidebar build(Player player);
}
