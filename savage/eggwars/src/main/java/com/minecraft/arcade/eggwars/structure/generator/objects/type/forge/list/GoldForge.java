package com.minecraft.arcade.eggwars.structure.generator.objects.type.forge.list;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import com.minecraft.arcade.eggwars.structure.generator.objects.type.forge.ForgeGenerator;
import com.minecraft.arcade.eggwars.structure.team.Team;
import org.bukkit.Location;

import java.util.concurrent.TimeUnit;

public class GoldForge extends ForgeGenerator {

    public GoldForge(Arena arena, Team team) {
        super(EggOre.GOLD, arena, team, TimeUnit.SECONDS.toMillis(5));

        setMaxStackSize(16);
    }

    @Override
    public void spawn() {
        Location center = getLocation();

        if (center != null)
            drop(getArena(), center);
    }
}
