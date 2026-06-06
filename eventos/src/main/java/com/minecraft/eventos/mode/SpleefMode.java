package com.minecraft.eventos.mode;

import com.minecraft.eventos.engine.EventModeRules;
import com.minecraft.eventos.engine.EventSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/** Spleef — quebre neve; queda elimina. */
public final class SpleefMode extends AbstractEliminationMode {

    @Override
    public String id() { return "spleef"; }

    @Override
    public String displayName() { return "Spleef"; }

    @Override
    public String defaultKitId() { return "spleef"; }

    @Override
    public EventModeRules rules() {
        return EventModeRules.spleef();
    }

    @Override
    public void onBlockBreak(EventSession session, BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type != Material.SNOW_BLOCK && type != Material.SNOW) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerMove(EventSession session, Player player, int dieY) {
        super.onPlayerMove(session, player, dieY(session));
    }
}
