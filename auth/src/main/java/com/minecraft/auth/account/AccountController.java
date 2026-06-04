package com.minecraft.auth.account;

import com.minecraft.auth.Core;
import com.minecraft.auth.backend.model.PlayerAuth;
import com.minecraft.auth.service.AuthService;
import com.minecraft.auth.util.AuthAccountSanitizer;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache em memoria das contas online. Credenciais ficam em {@code player_auth}.
 */
public final class AccountController {

    private final ConcurrentHashMap<UUID, Account> cache = new ConcurrentHashMap<>();

    public Account of(UUID id) {
        return cache.get(id);
    }

    public Collection<Account> list() {
        return Collections.unmodifiableCollection(cache.values());
    }

    public CompletableFuture<Account> load(UUID id, String name) {
        Account cached = cache.get(id);
        if (cached != null) {
            cached.setName(name);
            return CompletableFuture.completedFuture(cached);
        }

        return Core.getAccountData().loadAsync(id).thenApply(opt -> {
            Account account = opt.orElseGet(() -> {
                Account fresh = new Account(id, name);
                Core.getAccountData().saveAsync(fresh);
                return fresh;
            });
            account.setName(name);

            PlayerAuth auth;
            var authData = Core.getAuthData();
            if (authData != null) {
                auth = authData.migrateFromContextIfNeeded(account)
                        .orElseGet(() -> authData.load(id).orElseGet(() -> PlayerAuth.empty(id)));
            } else {
                auth = PlayerAuth.empty(id);
            }
            AuthService.getInstance().bind(id, auth);

            cache.put(id, account);
            return account;
        });
    }

    public void unload(UUID id) {
        Account removed = cache.remove(id);
        if (removed != null && removed.isDirty()) {
            persist(removed);
            removed.clearDirty();
        }
    }

    public void saveAllSync() {
        for (Account account : cache.values()) {
            try {
                persist(account);
                account.clearDirty();
            } catch (Throwable t) {
                Core.getLogger().warning("Falha ao salvar " + account.getName() + ": " + t.getMessage());
            }
        }
    }

    public void unloadAll() {
        saveAllSync();
        cache.clear();
    }

    private static void persist(Account account) {
        AuthAccountSanitizer.stripSecrets(account.getContext().getAuth());
        Core.getAccountData().save(account);
    }
}
