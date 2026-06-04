package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.block.BlockedPlayer;
import com.minecraft.lobbyhost.util.AccountSync;

import java.util.UUID;

public final class BlockListService {

    private BlockListService() {}

    public static boolean isBlocked(Account account, UUID otherId) {
        if (account == null || otherId == null) return false;
        return account.getContext().getBlocks().stream()
                .anyMatch(b -> b.getId() != null && b.getId().equals(otherId));
    }

    public static void block(Account account, UUID targetId) {
        if (account == null || targetId == null || account.getId().equals(targetId)) return;
        if (isBlocked(account, targetId)) return;
        account.getContext().getBlocks().add(new BlockedPlayer(targetId));
        account.markDirty();
        AccountSync.saveAndBroadcast(account);
    }

    public static void unblock(Account account, UUID targetId) {
        if (account == null || targetId == null) return;
        account.getContext().getBlocks().removeIf(b -> b.getId() != null && b.getId().equals(targetId));
        account.markDirty();
        AccountSync.saveAndBroadcast(account);
    }

    public static int count(Account account) {
        return account == null ? 0 : account.getContext().getBlocks().size();
    }
}
