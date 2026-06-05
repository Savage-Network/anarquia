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

public final class LoginCommand implements CommandInheritor {

    @Command(name = "login", aliases = {"logar", "entrar"}, playerOnly = true,
            description = "Faz login na sua conta", usage = "/login <senha>")
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

        if (ctx.getArgs().length < 1) {
            ctx.reply(AuthMessages.raw("auth.login-usage"));
            return;
        }

        AuthService.LoginResult result = AuthService.getInstance().login(account, ctx.getArgs()[0]);
        switch (result) {
            case OK -> {
                ctx.reply(AuthMessages.raw("auth.login-success"));
                AuthRedirects.toHub(player);
            }
            case NOT_REGISTERED -> ctx.reply(AuthMessages.raw("auth.not-registered"));
            case WRONG_PASSWORD -> ctx.reply(AuthMessages.format("auth.wrong-password",
                    Map.of("attempts", String.valueOf(AuthService.getInstance().getAttemptsLeft(player.getUniqueId())))));
            case KICK -> player.kick(AuthMessages.component("auth.kick-attempts"));
        }
    }
}
