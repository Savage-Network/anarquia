package com.minecraft.eventos.mode;

import com.minecraft.eventos.engine.EventModeRules;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.arena.ArenaDefinition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Block Party — blocos errados somem; sobreviva nas rodadas. */
public final class BlockPartyMode extends AbstractEliminationMode {

    private static final List<Material> COLORS = List.of(
            Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.LIME_CONCRETE,
            Material.YELLOW_CONCRETE, Material.MAGENTA_CONCRETE, Material.CYAN_CONCRETE);

    @Override
    public String id() { return "blockparty"; }

    @Override
    public String displayName() { return "Block Party"; }

    @Override
    public EventModeRules rules() {
        return new EventModeRules(false, false, false, false, false, false, false, false);
    }

    @Override
    public void onLive(EventSession session) {
        nextRound(session);
    }

    @Override
    public void onTick(EventSession session, int liveSeconds) {
        if (liveSeconds > 0 && liveSeconds % 8 == 0) {
            nextRound(session);
        }
    }

    private void nextRound(EventSession session) {
        int idx = session.getContext().getBlockPartyRound() % COLORS.size();
        Material safe = COLORS.get(idx);
        session.getContext().setBlockPartyRound(session.getContext().getBlockPartyRound() + 1);
        session.getContext().setBlockPartySafeColor(safe.name().toLowerCase(Locale.ROOT));

        ArenaDefinition arena = session.getArena();
        if (arena == null || arena.center() == null) return;
        Location center = arena.center();
        int r = arena.getRadius();
        int y = center.getBlockY();

        for (int x = center.getBlockX() - r; x <= center.getBlockX() + r; x++) {
            for (int z = center.getBlockZ() - r; z <= center.getBlockZ() + r; z++) {
                Block b = center.getWorld().getBlockAt(x, y, z);
                if (b.getType() == Material.AIR || b.getType() == safe) continue;
                b.setType(Material.AIR, false);
            }
        }

        session.broadcast("event.mode.blockparty.round", java.util.Map.of(
                "color", formatColor(safe),
                "round", String.valueOf(session.getContext().getBlockPartyRound())
        ));

        // elimina quem esta no ar ou em bloco errado
        for (Player p : session.aliveParticipants()) {
            Block under = p.getLocation().subtract(0, 0.1, 0).getBlock();
            if (under.getType() != safe && under.getType() != Material.AIR) {
                session.eliminate(p, "blockparty");
            } else if (p.getLocation().getBlockY() < y - 1) {
                session.eliminate(p, "queda");
            }
        }
    }

    private String formatColor(Material m) {
        return m.name().replace("_CONCRETE", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public Optional<UUID> checkWinner(EventSession session) {
        return super.checkWinner(session);
    }
}
