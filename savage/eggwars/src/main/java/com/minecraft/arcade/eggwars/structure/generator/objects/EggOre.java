package com.minecraft.arcade.eggwars.structure.generator.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum EggOre {
    IRON("Ferro", Material.IRON_INGOT, "§f"),
    GOLD("Ouro", Material.GOLD_INGOT, "§e"),
    DIAMOND("Diamante", Material.DIAMOND, "§b");

    private final String oreName;
    private final Material material;
    private final String color;
}
