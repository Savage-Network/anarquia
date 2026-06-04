package com.minecraft.arcade.eggwars.structure.team.objects.upgrade.structure.trap.action;

import com.minecraft.arcade.eggwars.structure.team.Team;
import org.bukkit.entity.Player;

public interface TrapAction {
    void execute(Player executor, Team team);
}
