package com.minecraft.eventos.mode;

import com.minecraft.eventos.engine.EventSession;
import org.bukkit.entity.Player;

/** Sumo — eliminacao por queda; kit knockback. */
public final class SumoMode extends AbstractEliminationMode {

    @Override
    public String id() { return "sumo"; }

    @Override
    public String displayName() { return "Sumo"; }

    @Override
    public String defaultKitId() { return "sumo"; }

    @Override
    public void onPlayerMove(EventSession session, Player player, int dieY) {
        super.onPlayerMove(session, player, dieY(session));
    }
}
