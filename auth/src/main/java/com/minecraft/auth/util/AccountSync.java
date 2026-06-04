package com.minecraft.auth.util;

import com.minecraft.auth.Constant;
import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;

public final class AccountSync {

    private AccountSync() {}

    public static void broadcastUpdate(Account account) {
        if (account == null || Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        Core.getRedis().publish(Constant.REDIS_ACCOUNT_UPDATE_CHANNEL, account.getId().toString());
    }

    public static void saveAndBroadcast(Account account) {
        if (account == null) return;
        Core.getAccountData().saveAsync(account).thenRun(() -> {
            account.clearDirty();
            broadcastUpdate(account);
        });
    }
}
