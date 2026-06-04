package com.minecraft.arcade.eggwars.arcade.arena.context;

import com.minecraft.arcade.eggwars.arcade.arena.context.objects.event.Event;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.Top;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.type.EggDestructionTop;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.type.FinalKillTop;
import com.minecraft.arcade.eggwars.structure.generator.objects.type.ore.OreGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ArenaContext {

    private Event event;

    private final List<Top> tops = new ArrayList<>(Arrays.asList(new FinalKillTop(), new EggDestructionTop()));

    private final List<OreGenerator> generators = new ArrayList<>();

    private boolean counterEnabled = true;
    
    private boolean ignoreMinPlayers = false;
    
    private boolean shouldCountStatistics = true;

    public Top getTop(TopType type) {
        return tops.stream().filter(top -> top.getType().equals(type)).findFirst().orElse(null);
    }
}
