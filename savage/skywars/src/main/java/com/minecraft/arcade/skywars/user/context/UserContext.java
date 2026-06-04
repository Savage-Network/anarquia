package com.minecraft.arcade.skywars.user.context;

import com.minecraft.core.api.reconnect.Reconnect;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserContext {

    private int kills = 0;
    private int xpGain = 0;
    private boolean alive = true;
    private Reconnect reconnect;

    public boolean hasReconnect() {
        return reconnect != null;
    }
}

