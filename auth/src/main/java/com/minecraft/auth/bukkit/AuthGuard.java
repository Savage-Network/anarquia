package com.minecraft.auth.bukkit;

import com.minecraft.auth.session.AuthSessionManager;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Bloqueio de ações enquanto o jogador não autenticou. */
public final class AuthGuard {

    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "login", "logar", "entrar",
            "register", "registrar", "registar",
            "changepassword", "trocarsenha"
    );

    private AuthGuard() {}

    public static boolean isLocked(UUID playerId) {
        return !AuthSessionManager.get().isAuthenticated(playerId);
    }

    public static boolean isLocked(Player player) {
        return isLocked(player.getUniqueId());
    }

    public static boolean isAllowedCommand(String message) {
        if (message == null || !message.startsWith("/")) return false;
        String cmd = message.substring(1).split(" ", 2)[0].toLowerCase(Locale.ROOT);
        return ALLOWED_COMMANDS.contains(cmd);
    }
}
