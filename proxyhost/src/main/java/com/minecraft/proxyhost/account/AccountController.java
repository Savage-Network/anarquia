package com.minecraft.proxyhost.account;

import com.minecraft.proxyhost.Core;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache em memoria das contas online no proxy. Espelha {@code AccountController}
 * do Savage. Operacoes de IO sao delegadas a {@link com.minecraft.proxyhost.backend.data.AccountData}.
 */
public final class AccountController {

    private final ConcurrentHashMap<UUID, Account> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> byName = new ConcurrentHashMap<>();

    public Account of(UUID id) { return cache.get(id); }

    public Account of(String name) {
        UUID id = byName.get(name.toLowerCase());
        return id == null ? null : cache.get(id);
    }

    public Collection<Account> list() { return Collections.unmodifiableCollection(cache.values()); }

    public CompletableFuture<Account> load(UUID id, String name) {
        Account cached = cache.get(id);
        if (cached != null) {
            cached.setName(name);
            byName.put(name.toLowerCase(), id);
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
            byName.put(name.toLowerCase(), id);
            return account;
        });
    }

    public void unload(UUID id) {
        Account removed = cache.remove(id);
        if (removed != null) {
            byName.remove(removed.getName().toLowerCase());
            if (removed.isDirty()) {
                Core.getAccountData().save(removed);
                removed.clearDirty();
            }
        }
    }

    public void saveAllSync() {
        for (Account account : cache.values()) {
            try {
                Core.getAccountData().save(account);
                account.clearDirty();
            } catch (Throwable t) {
                Core.getLogger().warn("Falha ao salvar " + account.getName() + ": " + t.getMessage());
            }
        }
    }

    public void unloadAll() {
        saveAllSync();
        cache.clear();
        byName.clear();
    }
}
