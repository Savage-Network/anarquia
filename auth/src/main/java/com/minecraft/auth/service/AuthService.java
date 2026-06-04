package com.minecraft.auth.service;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.account.context.objects.auth.AuthMetadata;
import com.minecraft.auth.account.context.objects.auth.AuthState;
import com.minecraft.auth.backend.data.AuthData;
import com.minecraft.auth.backend.model.PlayerAuth;
import com.minecraft.auth.util.AuthAccountSanitizer;
import lombok.Getter;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticacao com credenciais em {@code player_auth} (nao no JSON de {@code accounts}).
 */
public final class AuthService {

    @Getter
    private static final AuthService instance = new AuthService();

    private final Map<UUID, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> authenticated = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerAuth> authByPlayer = new ConcurrentHashMap<>();

    public String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Falha SHA-256", e);
        }
    }

    public void bind(UUID playerId, PlayerAuth auth) {
        if (auth == null) {
            authByPlayer.put(playerId, PlayerAuth.empty(playerId));
        } else {
            authByPlayer.put(playerId, auth);
        }
    }

    public PlayerAuth getAuth(UUID playerId) {
        return authByPlayer.computeIfAbsent(playerId, id -> {
            AuthData data = Core.getAuthData();
            if (data == null) return PlayerAuth.empty(id);
            return data.load(id).orElseGet(() -> PlayerAuth.empty(id));
        });
    }

    public boolean isAuthenticated(UUID id) {
        return authenticated.containsKey(id);
    }

    public boolean isRegistered(Account account) {
        return getAuth(account.getId()).isRegistered();
    }

    public LoginResult login(Account account, String password) {
        PlayerAuth auth = getAuth(account.getId());
        if (!auth.isRegistered()) {
            return LoginResult.NOT_REGISTERED;
        }
        String hash = hash(password);
        if (!hash.equals(auth.getPasswordHash())) {
            int left = attempts.merge(account.getId(), -1, Integer::sum);
            if (left <= 0) {
                attempts.remove(account.getId());
                return LoginResult.KICK;
            }
            return LoginResult.WRONG_PASSWORD;
        }

        auth.setLastLoginAt(Instant.now());
        persistAuth(auth);

        AuthMetadata meta = account.getContext().getAuth();
        meta.setState(AuthState.LOGIN);
        meta.setTimestamp(System.currentTimeMillis());
        AuthAccountSanitizer.stripSecrets(meta);
        account.markDirty();

        authenticated.put(account.getId(), System.currentTimeMillis());
        attempts.remove(account.getId());
        return LoginResult.OK;
    }

    public RegisterResult register(Account account, String password, String confirm, int minLength) {
        if (isRegistered(account)) return RegisterResult.ALREADY_REGISTERED;
        if (password == null || password.length() < minLength) return RegisterResult.TOO_SHORT;
        if (!password.equals(confirm)) return RegisterResult.MISMATCH;

        PlayerAuth auth = getAuth(account.getId());
        String newHash = hash(password);
        auth.setLastPasswordHash(auth.getPasswordHash());
        auth.setPasswordHash(newHash);
        if (auth.getRecentHashes() == null) auth.setRecentHashes(new java.util.ArrayList<>());
        auth.getRecentHashes().add(newHash);
        if (auth.getRegisteredAt() == null) auth.setRegisteredAt(Instant.now());
        auth.setLastLoginAt(Instant.now());
        persistAuth(auth);

        AuthMetadata meta = account.getContext().getAuth();
        meta.setState(AuthState.LOGIN);
        meta.setTimestamp(System.currentTimeMillis());
        AuthAccountSanitizer.stripSecrets(meta);
        account.markDirty();

        authenticated.put(account.getId(), System.currentTimeMillis());
        return RegisterResult.OK;
    }

    public ChangePasswordResult changePassword(Account account, String current, String next, int minLength) {
        if (!isRegistered(account)) return ChangePasswordResult.NOT_REGISTERED;
        if (next == null || next.length() < minLength) return ChangePasswordResult.TOO_SHORT;

        PlayerAuth auth = getAuth(account.getId());
        if (!hash(current).equals(auth.getPasswordHash())) {
            return ChangePasswordResult.WRONG_PASSWORD;
        }

        String newHash = hash(next);
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

    private void persistAuth(PlayerAuth auth) {
        AuthData data = Core.getAuthData();
        if (data != null) data.save(auth);
    }

    public void initAttempts(UUID id, int max) {
        attempts.put(id, max);
    }

    public int getAttemptsLeft(UUID id) {
        return attempts.getOrDefault(id, 0);
    }

    public void clear(UUID id) {
        authenticated.remove(id);
        attempts.remove(id);
        authByPlayer.remove(id);
    }

    public void markSessionAuthenticated(Account account) {
        AuthMetadata meta = account.getContext().getAuth();
        meta.setState(AuthState.LOGIN);
        meta.setTimestamp(System.currentTimeMillis());
        AuthAccountSanitizer.stripSecrets(meta);
        account.markDirty();
        authenticated.put(account.getId(), System.currentTimeMillis());
        attempts.remove(account.getId());
    }

    public enum LoginResult {
        OK, WRONG_PASSWORD, NOT_REGISTERED, KICK
    }

    public enum RegisterResult {
        OK, ALREADY_REGISTERED, TOO_SHORT, MISMATCH
    }

    public enum ChangePasswordResult {
        OK, WRONG_PASSWORD, TOO_SHORT, REUSED, NOT_REGISTERED
    }
}
