package com.minecraft.arcade.event.game.team;

import com.minecraft.core.arcade.room.team.preset.TeamPresetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Team extends com.minecraft.core.arcade.room.team.Team
{

    private boolean unlocked = false;

    public Team(TeamPresetType preset)
    {
        super(preset.getName(), preset.getId(), preset.getColor(), preset.getRgb(), null, null, 2);
    }
}
