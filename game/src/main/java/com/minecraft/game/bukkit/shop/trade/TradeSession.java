package com.minecraft.game.bukkit.shop.trade;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class TradeSession {

    private final UUID id;
    private final UUID playerA;
    private final UUID playerB;
    private final ItemStack[] offerA = new ItemStack[12];
    private final ItemStack[] offerB = new ItemStack[12];
    private boolean confirmA;
    private boolean confirmB;

    public TradeSession(UUID id, UUID playerA, UUID playerB) {
        this.id = id;
        this.playerA = playerA;
        this.playerB = playerB;
    }

    public UUID id() { return id; }
    public UUID playerA() { return playerA; }
    public UUID playerB() { return playerB; }

    public ItemStack[] offer(UUID player) {
        return player.equals(playerA) ? offerA : offerB;
    }

    public boolean isParticipant(UUID player) {
        return player.equals(playerA) || player.equals(playerB);
    }

    public Player other(Player p) {
        var server = org.bukkit.Bukkit.getServer();
        if (p.getUniqueId().equals(playerA)) {
            return server.getPlayer(playerB);
        }
        return server.getPlayer(playerA);
    }

    public void toggleConfirm(UUID player) {
        if (player.equals(playerA)) confirmA = !confirmA;
        else confirmB = !confirmB;
    }

    public boolean bothConfirmed() {
        return confirmA && confirmB;
    }

    public boolean isConfirmed(UUID player) {
        return player.equals(playerA) ? confirmA : confirmB;
    }
}
