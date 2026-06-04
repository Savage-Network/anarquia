package com.minecraft.game.account;

import com.minecraft.game.Core;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache em memoria das contas online. Espelha {@code AccountController}
 * do Savage. Operacoes de IO sao delegadas a {@link com.minecraft.game.backend.data.AccountData}.
 */
public final class AccountController {

    private final ConcurrentHashMap<UUID, Account> cache = new ConcurrentHashMap<>();

    public Account of(UUID id) {
        return cache.get(id);
    }

    public Collection<Account> list() {
        return Collections.unmodifiableCollection(cache.values());
    }

    /**
     * Carrega a conta de forma async. Se nao existir no banco, cria uma nova.
     */
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
            cache.put(id, account);
            return account;
        });
    }

    public void unload(UUID id) {
        Account removed = cache.remove(id);
        if (removed != null && removed.isDirty()) {
            Core.getAccountData().save(removed);
            removed.clearDirty();
        }
    }

    /** Salva todas as contas online de forma sincrona - usado no {@code onDisable}. */
    public void saveAllSync() {
        for (Account account : cache.values()) {
            try {
                Core.getAccountData().save(account);
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
}
