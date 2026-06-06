package com.minecraft.eventos.mode;

import com.minecraft.eventos.engine.EventMode;
import com.minecraft.eventos.engine.EventModeRules;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.game.variable.Variable;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/** Base para modos de eliminacao (ultimo vivo). */
public abstract class AbstractEliminationMode implements EventMode {

    @Override
    public EventModeRules rules() {
        return EventModeRules.pvpArena();
    }

    @Override
    public void onSessionBound(EventSession session) {
        session.setSumo("sumo".equals(id()));
    }

    @Override
    public void onLive(EventSession session) { }

    @Override
    public void onTick(EventSession session, int liveSeconds) { }

    @Override
    public void onEnd(EventSession session) { }

    @Override
    public void onPlayerMove(EventSession session, Player player, int dieY) {
        if (player.getLocation().getBlockY() < dieY) {
            session.eliminate(player, "queda");
        }
    }

    @Override
    public Optional<UUID> checkWinner(EventSession session) {
        var alive = session.aliveParticipants();
        if (alive.isEmpty()) {
            var all = session.alives();
            if (all.size() == 1) return Optional.of(all.get(0).getUniqueId());
            return Optional.empty();
        }
        if (alive.size() == 1) return Optional.of(alive.get(0).getUniqueId());
        return Optional.empty();
    }

    protected int dieY(EventSession session) {
        if (session.getArena() != null) return session.getArena().getDieY();
        return Variable.getInt(Variable.Key.DIE_Y);
    }
}
