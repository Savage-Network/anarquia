package com.minecraft.eventos.mode;

import com.minecraft.eventos.engine.EventModeRules;
import com.minecraft.eventos.engine.EventSession;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

/** FFA Deathmatch — mais kills vence (timer via variavel ENDING). */
public final class FfaMode implements com.minecraft.eventos.engine.EventMode {

    private static final int MATCH_DURATION = 120;

    @Override
    public String id() { return "ffa"; }

    @Override
    public String displayName() { return "Deathmatch"; }

    @Override
    public EventModeRules rules() {
        return EventModeRules.ffa();
    }

    @Override
    public String defaultKitId() { return "ffa"; }

    @Override
    public void onSessionBound(EventSession session) { }

    @Override
    public void onLive(EventSession session) { }

    @Override
    public void onTick(EventSession session, int liveSeconds) {
        if (liveSeconds >= MATCH_DURATION) {
            UUID top = session.aliveParticipants().stream()
                    .max(Comparator.comparingInt(p -> session.getContext().killsOf(p.getUniqueId())))
                    .map(Player::getUniqueId)
                    .orElse(null);
            session.end(top);
        }
    }

    @Override
    public void onEnd(EventSession session) { }

    @Override
    public void onDamage(EventSession session, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player killer)) return;
        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            session.getContext().addKill(killer.getUniqueId());
            session.broadcast("event.mode.ffa.kill", java.util.Map.of(
                    "killer", killer.getName(),
                    "victim", victim.getName(),
                    "kills", String.valueOf(session.getContext().killsOf(killer.getUniqueId()))
            ));
        }
    }

    @Override
    public Optional<UUID> checkWinner(EventSession session) {
        return Optional.empty();
    }
}
