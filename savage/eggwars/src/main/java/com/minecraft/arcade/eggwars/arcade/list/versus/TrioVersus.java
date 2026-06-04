package com.minecraft.arcade.eggwars.arcade.list.versus;

import com.minecraft.arcade.eggwars.arcade.Arcade;
import com.minecraft.core.arcade.category.ArcadeCategory;

public class TrioVersus extends Arcade {

    public TrioVersus(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.EGGWARS_VERSUS_TRIO);
    }
}
