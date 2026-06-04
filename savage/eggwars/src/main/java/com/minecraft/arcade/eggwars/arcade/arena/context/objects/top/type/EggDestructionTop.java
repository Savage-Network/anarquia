package com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.type;

import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.Top;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.arcade.eggwars.user.User;

public class EggDestructionTop extends Top {

    public EggDestructionTop() {
        super(TopType.EGG_DESTRUCTION);
    }

    @Override
    public int getStatistic(User user) {
        return user.getContext().getEggDestruction();
    }
}
