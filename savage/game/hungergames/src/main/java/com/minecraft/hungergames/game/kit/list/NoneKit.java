package com.minecraft.hungergames.game.kit.list;

import com.minecraft.hungergames.game.kit.Kit;
import com.minecraft.hungergames.game.kit.KitCategory;

public class NoneKit extends Kit {

    public NoneKit() {
        define("Nenhum", "§7Nenhum", "");
        setCategory(KitCategory.DEFAULT);
        setPrice(0);
    }

}
