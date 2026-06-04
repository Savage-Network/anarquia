package com.minecraft.arcade.skywars.arcade.list.normal;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.core.arcade.category.ArcadeCategory;

public class Duo extends Arcade {

    public Duo(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.SKYWARS_DUO);
    }
}

