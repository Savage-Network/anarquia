package com.minecraft.game.util;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;

/** Propaga alteracoes de conta entre servidores (Redis). */
public final class AccountSync {

    private AccountSync() {}

    public static void broadcastUpdate(Account account) {
        if (account == null || Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        Core.getRedis().publish(Constant.REDIS_ACCOUNT_UPDATE_CHANNEL, account.getId().toString());
    }

    /** Persiste no banco e so entao notifica os outros servidores (evita race no reload). */
    public static void saveAndBroadcast(Account account) {
        if (account == null) return;
        Core.getAccountData().saveWithIdentityAsync(account).thenRun(() -> {
            account.clearDirty();
            broadcastUpdate(account);
        });
    }
}
