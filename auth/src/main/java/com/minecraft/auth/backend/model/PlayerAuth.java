package com.minecraft.auth.backend.model;

import com.minecraft.auth.account.context.objects.auth.AuthMetadata;
import com.minecraft.auth.account.context.objects.auth.AuthState;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Credenciais e historico de senha — persistidos apenas em {@code player_auth}.
 */
@Getter
@Setter
public class PlayerAuth {

    private UUID playerId;
    private String passwordHash = "";
    private String lastPasswordHash = "";
    private List<String> recentHashes = new ArrayList<>();
    private Instant registeredAt;
    private Instant lastLoginAt;

    public static PlayerAuth empty(UUID playerId) {
        PlayerAuth auth = new PlayerAuth();
        auth.playerId = playerId;
        return auth;
    }

    public static PlayerAuth fromLegacy(UUID playerId, AuthMetadata meta) {
        PlayerAuth auth = empty(playerId);
        if (meta.getPassword() != null) auth.passwordHash = meta.getPassword();
        if (meta.getLastPassword() != null) auth.lastPasswordHash = meta.getLastPassword();
        if (meta.getRecentPasswords() != null) {
            auth.recentHashes = new ArrayList<>(meta.getRecentPasswords());
        }
        if (meta.getTimestamp() > 0L) {
            auth.registeredAt = Instant.ofEpochMilli(meta.getTimestamp());
        } else {
            auth.registeredAt = Instant.now();
        }
        return auth;
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }

    public boolean isRegistered() {
        return hasPassword();
    }

    public AuthState persistedState() {
        return isRegistered() ? AuthState.LOGIN : AuthState.REGISTER;
    }
}
