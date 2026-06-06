package com.minecraft.eventos.mode;

import com.minecraft.eventos.engine.EventSession;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.minecraft.eventos.EventosPlugin;

/** TNT Run — chao desaparece ao pisar. */
public final class TntRunMode extends AbstractEliminationMode {

    @Override
    public String id() { return "tntrun"; }

    @Override
    public String displayName() { return "TNT Run"; }

    @Override
    public String defaultKitId() { return "tntrun"; }

    @Override
    public void onLive(EventSession session) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (session.getState() != com.minecraft.eventos.game.state.GameState.PLAYING) {
                    cancel();
                    return;
                }
                for (Player p : session.aliveParticipants()) {
                    Block below = p.getLocation().subtract(0, 1, 0).getBlock();
                    Material t = below.getType();
                    if (t == Material.SAND || t == Material.GRAVEL || t == Material.CLAY) {
                        EventosPlugin.getInstance().getServer().getScheduler().runTaskLater(
                                EventosPlugin.getInstance(),
                                () -> below.setType(Material.AIR, false),
                                6L);
                    }
                }
            }
        }.runTaskTimer(EventosPlugin.getInstance(), 10L, 2L);
    }

    @Override
    public void onPlayerMove(EventSession session, Player player, int dieY) {
        super.onPlayerMove(session, player, dieY(session));
    }
}
