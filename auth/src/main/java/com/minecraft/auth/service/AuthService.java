package com.minecraft.auth.service;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.account.context.objects.auth.AuthMetadata;
import com.minecraft.auth.account.context.objects.auth.AuthState;
import com.minecraft.auth.backend.data.AuthData;
import com.minecraft.auth.backend.model.PlayerAuth;
import com.minecraft.auth.config.AuthConfig;
import com.minecraft.auth.security.PasswordHasher;
import com.minecraft.auth.session.AuthSessionManager;
import com.minecraft.auth.util.AuthAccountSanitizer;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço central de autenticação.
 * Credenciais em {@code player_auth}; estado de sessão em {@link AuthSessionManager}.
 */
public final class AuthService {

    @Getter
    private static final AuthService instance = new AuthService();

    private final Map<UUID, PlayerAuth> authCache = new ConcurrentHashMap<>();

    private AuthSessionManager sessions() {
        return AuthSessionManager.get();
    }

    public void bind(UUID playerId, PlayerAuth auth) {
        authCache.put(playerId, auth != null ? auth : PlayerAuth.empty(playerId));
    }

    public PlayerAuth getAuth(UUID playerId) {
        return authCache.computeIfAbsent(playerId, id -> {
            AuthData data = Core.getAuthData();
            if (data == null) return PlayerAuth.empty(id);
            return data.load(id).orElseGet(() -> PlayerAuth.empty(id));
        });
    }

    public boolean isAuthenticated(UUID id) {
        return sessions().isAuthenticated(id);
    }

    public boolean isRegistered(Account account) {
        return getAuth(account.getId()).isRegistered();
    }

    public void initAttempts(UUID id) {
        sessions().initAttempts(id, AuthConfig.get().getMaxLoginAttempts());
    }

    public int getAttemptsLeft(UUID id) {
        return sessions().getAttemptsLeft(id);
    }

    public void clear(UUID id) {
        sessions().clear(id);
        authCache.remove(id);
    }

    public void markSessionAuthenticated(Account account) {
        completeAuth(account);
    }

    public LoginResult login(Account account, String password) {
        UUID id = account.getId();
        PlayerAuth auth = getAuth(id);

        if (!auth.isRegistered()) {
            return LoginResult.NOT_REGISTERED;
        }
        if (!PasswordHasher.verify(id, password, auth.getPasswordHash())) {
            int left = sessions().decrementAttempts(id);
            if (left <= 0) {
                sessions().clearAttempts(id);
                return LoginResult.KICK;
            }
            return LoginResult.WRONG_PASSWORD;
        }

        String upgraded = PasswordHasher.hash(id, password);
        if (!upgraded.equals(auth.getPasswordHash())) {
            auth.setPasswordHash(upgraded);
        }

        auth.setLastLoginAt(Instant.now());
        persistAuth(auth);
        completeAuth(account);
        return LoginResult.OK;
    }

    public RegisterResult register(Account account, String password, String confirm) {
        if (isRegistered(account)) return RegisterResult.ALREADY_REGISTERED;

        int min = AuthConfig.get().getMinPasswordLength();
        if (password == null || password.length() < min) return RegisterResult.TOO_SHORT;
        if (!password.equals(confirm)) return RegisterResult.MISMATCH;

        UUID id = account.getId();
        PlayerAuth auth = getAuth(id);
        String newHash = PasswordHasher.hash(id, password);

        auth.setLastPasswordHash(auth.getPasswordHash());
        auth.setPasswordHash(newHash);
        if (auth.getRecentHashes() == null) auth.setRecentHashes(new java.util.ArrayList<>());
        auth.getRecentHashes().add(newHash);
        if (auth.getRegisteredAt() == null) auth.setRegisteredAt(Instant.now());
        auth.setLastLoginAt(Instant.now());
        persistAuth(auth);

        completeAuth(account);
        return RegisterResult.OK;
    }

    public ChangePasswordResult changePassword(Account account, String current, String next) {
        if (!isRegistered(account)) return ChangePasswordResult.NOT_REGISTERED;

        int min = AuthConfig.get().getMinPasswordLength();
        if (next == null || next.length() < min) return ChangePasswordResult.TOO_SHORT;

        UUID id = account.getId();
        PlayerAuth auth = getAuth(id);
        if (!PasswordHasher.verify(id, current, auth.getPasswordHash())) {
            return ChangePasswordResult.WRONG_PASSWORD;
        }

        String newHash = PasswordHasher.hash(id, next);
        if (auth.getRecentHashes() != null && auth.getRecentHashes().contains(newHash)) {
            return ChangePasswordResult.REUSED;
        }

        auth.setLastPasswordHash(auth.getPasswordHash());
        auth.setPasswordHash(newHash);
        if (auth.getRecentHashes() == null) auth.setRecentHashes(new java.util.ArrayList<>());
        auth.getRecentHashes().add(newHash);
        persistAuth(auth);

        AuthMetadata meta = account.getContext().getAuth();
        meta.setTimestamp(System.currentTimeMillis());
        AuthAccountSanitizer.stripSecrets(meta);
        account.markDirty();
        return ChangePasswordResult.OK;
    }

    private void completeAuth(Account account) {
        AuthMetadata meta = account.getContext().getAuth();
        meta.setState(AuthState.LOGIN);
        meta.setTimestamp(System.currentTimeMillis());
        AuthAccountSanitizer.stripSecrets(meta);
        account.markDirty();
        sessions().authenticate(account.getId());
    }

    private void persistAuth(PlayerAuth auth) {
        AuthData data = Core.getAuthData();
        if (data != null) data.save(auth);
    }

    public enum LoginResult { OK, WRONG_PASSWORD, NOT_REGISTERED, KICK }

    public enum RegisterResult { OK, ALREADY_REGISTERED, TOO_SHORT, MISMATCH }

    public enum ChangePasswordResult { OK, WRONG_PASSWORD, TOO_SHORT, REUSED, NOT_REGISTERED }
}
