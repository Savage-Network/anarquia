package com.minecraft.auth.bukkit.command.single;

import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.command.annotation.Command;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.auth.bukkit.command.structure.CommandInheritor;
import com.minecraft.auth.config.AuthConfig;
import com.minecraft.auth.message.AuthMessages;
import com.minecraft.auth.service.AuthService;
import com.minecraft.auth.util.AuthRedirects;
import com.minecraft.auth.util.PlayerOriginDetector;
import org.bukkit.entity.Player;

import java.util.Map;

public final class RegisterCommand implements CommandInheritor {

    @Command(name = "register", aliases = {"registrar", "registar"}, playerOnly = true,
            description = "Registra uma nova conta",
            usage = "/register <senha> <confirmar>")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply(AuthMessages.raw("account.load-failed"));
            return;
        }

        if (PlayerOriginDetector.isMojangOriginal(player)) {
            ctx.reply(AuthMessages.raw("auth.original-no-auth"));
            AuthRedirects.toHub(player);
            return;
        }

        if (AuthService.getInstance().isAuthenticated(player.getUniqueId())) {
            ctx.reply(AuthMessages.raw("auth.already-authenticated"));
            return;
        }

        if (ctx.getArgs().length < 2) {
            ctx.reply(AuthMessages.raw("auth.register-usage"));
            return;
        }

        AuthService.RegisterResult result = AuthService.getInstance()
                .register(account, ctx.getArgs()[0], ctx.getArgs()[1]);

        switch (result) {
            case OK -> {
                ctx.reply(AuthMessages.format("auth.register-success",
                        Map.of("player", player.getName())));
                AuthRedirects.toHub(player);
            }
            case ALREADY_REGISTERED -> ctx.reply(AuthMessages.raw("auth.already-registered"));
            case TOO_SHORT -> ctx.reply(AuthMessages.format("auth.password-too-short",
                    Map.of("min", String.valueOf(AuthConfig.get().getMinPasswordLength()))));
            case MISMATCH -> ctx.reply(AuthMessages.raw("auth.password-mismatch"));
        }
    }
}
