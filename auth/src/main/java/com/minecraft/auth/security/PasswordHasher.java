package com.minecraft.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/** Hash de senha com compatibilidade legado (SHA-256 puro) e salted (UUID). */
public final class PasswordHasher {

    private PasswordHasher() {}

    public static String hash(UUID playerId, String password) {
        return sha256(playerId + ":" + password);
    }

    public static boolean verify(UUID playerId, String password, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) return false;
        if (storedHash.equals(sha256(password))) return true;
        return storedHash.equals(hash(playerId, password));
    }

    /** Re-hash após login legado bem-sucedido. */
    public static String upgrade(UUID playerId, String password) {
        return hash(playerId, password);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
