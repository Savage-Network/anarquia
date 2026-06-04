package com.minecraft.arcade.eggwars.structure.generator.objects.type.ore.list;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.generator.config.GeneratorConfig;
import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.eggwars.structure.generator.objects.type.ore.OreGenerator;
import org.bukkit.Material;

public class DiamondGenerator extends OreGenerator {

    private static final GeneratorConfig config = GeneratorConfig.getInstance();
    private final boolean isMiddleGenerator;

    public DiamondGenerator(int id, Arena arena) {
        this(id, arena, false);
    }

    public DiamondGenerator(int id, Arena arena, boolean isMiddleGenerator) {
        super(id, arena, EggOre.DIAMOND, 
              config.getGenerationTime(EggOre.DIAMOND, GeneratorLevel.ONE, isMiddleGenerator), 
              config.startsBroken(EggOre.DIAMOND, isMiddleGenerator));

        this.isMiddleGenerator = isMiddleGenerator;
        setMaxStackSize(config.getMaxStackSize(EggOre.DIAMOND));
        updateTimeForLevel(); // Aplicar tempo inicial correto
    }

    @Override
    public void spawn() {
        // Só spawnar se não estiver quebrado
        if (getLevel() != GeneratorLevel.NONE) {
        drop(getArena(), getLocation());
        }
    }
    
    @Override
    public int getUpdateTimeForLevel(GeneratorLevel level) {
        return config.getGenerationTime(EggOre.DIAMOND, level, isMiddleGenerator);
    }
    
    @Override
    public int getUpgradeCost(GeneratorLevel nextLevel) {
        return config.getUpgradeCost(EggOre.DIAMOND, nextLevel);
    }
    
    @Override
    public Material getUpgradeMaterial() {
        return config.getUpgradeMaterial(EggOre.DIAMOND);
    }
}
