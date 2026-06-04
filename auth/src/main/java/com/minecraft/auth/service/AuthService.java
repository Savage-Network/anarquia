package com.minecraft.auth.service;

import com.minecraft.auth.account.Account;
import com.minecraft.auth.account.context.objects.auth.AuthMetadata;
import com.minecraft.auth.account.context.objects.auth.AuthState;
import lombok.Getter;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logica de autenticacao espelhada de {@code auth/.../Auth.java} do Savage.
 *
 * <p>Estados:
 * <ul>
 *   <li>{@link AuthState#REGISTER} - nunca cadastrou senha</li>
 *   <li>{@link AuthState#LOGIN} - cadastrado, precisa logar</li>
 *   <li>{@link AuthState#OK} - autenticado nesta sessao</li>
 * </ul>
 */
public final class AuthService {

    @Getter
    private static final AuthService instance = new AuthService();

    /** Tentativas restantes de login por jogador. */
    private final Map<UUID, Integer> attempts = new ConcurrentHashMap<>();

    /** Jogadores autenticados nesta sessao. */
    private final Map<UUID, Long> authenticated = new ConcurrentHashMap<>();

    private final Map<UUID, String> pendingRegister = new HashMap<>();

    /* -------- hashing SHA-256 (simples, igual Savage) -------- */
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

    /* -------- API -------- */

    public boolean isAuthenticated(UUID id) {
        return authenticated.containsKey(id);
    }

    public boolean isRegistered(Account account) {
        AuthMetadata meta = account.getContext().getAuth();
        return meta.getState() == AuthState.LOGIN || meta.getState() == AuthState.OK
                || (meta.getPassword() != null && !meta.getPassword().isEmpty());
    }

    public LoginResult login(Account account, String password) {
        AuthMetadata meta = account.getContext().getAuth();
        if (meta.getPassword() == null || meta.getPassword().isEmpty()) {
            return LoginResult.NOT_REGISTERED;
        }
        String hash = hash(password);
        if (!hash.equals(meta.getPassword())) {
            int left = attempts.merge(account.getId(), -1, Integer::sum);
            if (left <= 0) {
                attempts.remove(account.getId());
                return LoginResult.KICK;
            }
            return LoginResult.WRONG_PASSWORD;
        }
        meta.setState(AuthState.OK);
        meta.setTimestamp(System.currentTimeMillis());
        account.markDirty();
        authenticated.put(account.getId(), System.currentTimeMillis());
        attempts.remove(account.getId());
        return LoginResult.OK;
    }

    public RegisterResult register(Account account, String password, String confirm, int minLength) {
        if (isRegistered(account)) return RegisterResult.ALREADY_REGISTERED;
        if (password == null || password.length() < minLength) return RegisterResult.TOO_SHORT;
        if (!password.equals(confirm)) return RegisterResult.MISMATCH;

        AuthMetadata meta = account.getContext().getAuth();
        meta.setLastPassword(meta.getPassword());
        meta.setPassword(hash(password));
        meta.setState(AuthState.OK);
        meta.setTimestamp(System.currentTimeMillis());
        meta.getRecentPasswords().add(meta.getPassword());
        account.markDirty();
        authenticated.put(account.getId(), System.currentTimeMillis());
        return RegisterResult.OK;
    }

    public ChangePasswordResult changePassword(Account account, String current, String next, int minLength) {
        if (!isRegistered(account)) return ChangePasswordResult.NOT_REGISTERED;
        if (next == null || next.length() < minLength) return ChangePasswordResult.TOO_SHORT;

        AuthMetadata meta = account.getContext().getAuth();
        if (!hash(current).equals(meta.getPassword())) return ChangePasswordResult.WRONG_PASSWORD;

        String newHash = hash(next);
        if (meta.getRecentPasswords().contains(newHash)) return ChangePasswordResult.REUSED;

        meta.setLastPassword(meta.getPassword());
        meta.setPassword(newHash);
        meta.setTimestamp(System.currentTimeMillis());
        meta.getRecentPasswords().add(newHash);
        account.markDirty();
        return ChangePasswordResult.OK;
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
        pendingRegister.remove(id);
    }

    /** Marca sessao autenticada (dev-mode / bypass). */
    public void markSessionAuthenticated(Account account) {
        AuthMetadata meta = account.getContext().getAuth();
        meta.setState(AuthState.OK);
        meta.setTimestamp(System.currentTimeMillis());
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
