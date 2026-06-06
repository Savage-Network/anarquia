package com.minecraft.lobbyhost.util;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;

public final class AccountSync {

    private AccountSync() {}

    public static void broadcastUpdate(Account account) {
        if (account == null || Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        Core.getRedis().publish(Constant.REDIS_ACCOUNT_UPDATE_CHANNEL, account.getId().toString());
    }

    public static void saveAndBroadcast(Account account) {
        if (account == null) return;
        Core.getAccountData().saveWithIdentityAsync(account).thenRun(() -> {
            account.clearDirty();
            broadcastUpdate(account);
        });
    }
}
