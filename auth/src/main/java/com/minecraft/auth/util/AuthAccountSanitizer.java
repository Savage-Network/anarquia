package com.minecraft.auth.util;

import com.minecraft.auth.account.context.objects.auth.AuthMetadata;

/**
 * Remove dados sensiveis do JSON da conta antes de persistir em {@code accounts}.
 */
public final class AuthAccountSanitizer {

    private AuthAccountSanitizer() {}

    public static void stripSecrets(AuthMetadata meta) {
        if (meta == null) return;
        meta.setPassword("");
        meta.setLastPassword("");
        if (meta.getRecentPasswords() != null) {
            meta.getRecentPasswords().clear();
        }
    }
}
