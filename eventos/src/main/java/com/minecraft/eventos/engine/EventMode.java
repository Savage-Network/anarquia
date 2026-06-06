package com.minecraft.eventos.engine;

import com.minecraft.eventos.game.team.TeamPresetType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Contrato de um modo de evento plugavel. */
public interface EventMode {

    String id();

    String displayName();

    EventModeRules rules();

    /** Kit padrao aplicado ao entrar em LIVE (nome em kits.yml ou built-in). */
    default String defaultKitId() {
        return id();
    }

    void onSessionBound(EventSession session);

    void onLive(EventSession session);

    void onTick(EventSession session, int liveSeconds);

    void onEnd(EventSession session);

    default void onPlayerJoin(EventSession session, Player player) {}

    default void onPlayerMove(EventSession session, Player player, int dieY) {}

    default void onBlockBreak(EventSession session, BlockBreakEvent event) {}

    default void onDamage(EventSession session, EntityDamageByEntityEvent event) {}

    default void onEliminate(EventSession session, Player player, String reason) {}

    /** Vencedor individual; vazio se empate ou vitoria por time. */
    Optional<UUID> checkWinner(EventSession session);

    /** Vitoria por time. */
    default Optional<TeamPresetType> checkWinningTeam(EventSession session) {
        return Optional.empty();
    }

    default List<String> scoreboardLines(EventSession session, Player viewer) {
        return List.of();
    }
}
