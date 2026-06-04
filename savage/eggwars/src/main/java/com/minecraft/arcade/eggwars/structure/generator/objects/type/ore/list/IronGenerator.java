package com.minecraft.arcade.eggwars.structure.generator.objects.type.ore.list;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.generator.config.GeneratorConfig;
import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.eggwars.structure.generator.objects.type.ore.OreGenerator;
import org.bukkit.Material;

public class IronGenerator extends OreGenerator {

    private static final GeneratorConfig config = GeneratorConfig.getInstance();

    public IronGenerator(int id, Arena arena) {
        super(id, arena, EggOre.IRON, config.getGenerationTime(EggOre.IRON, GeneratorLevel.ONE, false));

        setMaxStackSize(config.getMaxStackSize(EggOre.IRON));
        updateTimeForLevel(); // Aplicar tempo inicial correto
    }

    @Override
    public void spawn() {
        drop(getArena(), getLocation());
    }
    
    @Override
    public int getUpdateTimeForLevel(GeneratorLevel level) {
        return config.getGenerationTime(EggOre.IRON, level, false);
    }
    
    @Override
    public int getUpgradeCost(GeneratorLevel nextLevel) {
        return config.getUpgradeCost(EggOre.IRON, nextLevel);
    }
    
    @Override
    public Material getUpgradeMaterial() {
        return config.getUpgradeMaterial(EggOre.IRON);
    }
}
