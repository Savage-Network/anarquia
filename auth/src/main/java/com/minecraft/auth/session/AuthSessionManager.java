package com.minecraft.auth.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sessões autenticadas e tentativas de login (memória do servidor auth). */
public final class AuthSessionManager {

    private static final AuthSessionManager INSTANCE = new AuthSessionManager();

    private final Map<UUID, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> authenticatedAt = new ConcurrentHashMap<>();

    private AuthSessionManager() {}

    public static AuthSessionManager get() {
        return INSTANCE;
    }

    public void initAttempts(UUID id, int max) {
        attempts.put(id, max);
    }

    public int decrementAttempts(UUID id) {
        return attempts.merge(id, -1, Integer::sum);
    }

    public int getAttemptsLeft(UUID id) {
        return Math.max(0, attempts.getOrDefault(id, 0));
    }

    public void clearAttempts(UUID id) {
        attempts.remove(id);
    }

    public boolean isAuthenticated(UUID id) {
        return authenticatedAt.containsKey(id);
    }

    public void authenticate(UUID id) {
        authenticatedAt.put(id, System.currentTimeMillis());
        attempts.remove(id);
    }

    public void clear(UUID id) {
        authenticatedAt.remove(id);
        attempts.remove(id);
    }
}
