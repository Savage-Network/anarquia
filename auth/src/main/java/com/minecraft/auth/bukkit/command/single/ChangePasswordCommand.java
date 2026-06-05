package com.minecraft.auth.bukkit.command.single;

import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.AuthGuard;
import com.minecraft.auth.bukkit.command.annotation.Command;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.auth.bukkit.command.structure.CommandInheritor;
import com.minecraft.auth.config.AuthConfig;
import com.minecraft.auth.message.AuthMessages;
import com.minecraft.auth.service.AuthService;

import java.util.Map;

public final class ChangePasswordCommand implements CommandInheritor {

    @Command(name = "changepassword", aliases = {"trocarsenha"}, playerOnly = true,
            description = "Altera a senha da sua conta",
            usage = "/changepassword <senha_atual> <nova_senha>")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply(AuthMessages.raw("account.load-failed"));
            return;
        }

        if (AuthGuard.isLocked(ctx.player())) {
            ctx.reply(AuthMessages.raw("auth.must-login-first"));
            return;
        }

        if (ctx.getArgs().length < 2) {
            ctx.reply(AuthMessages.raw("auth.changepassword-usage"));
            return;
        }

        AuthService.ChangePasswordResult result = AuthService.getInstance()
                .changePassword(account, ctx.getArgs()[0], ctx.getArgs()[1]);

        switch (result) {
            case OK -> ctx.reply(AuthMessages.raw("auth.changepassword-success"));
            case NOT_REGISTERED -> ctx.reply(AuthMessages.raw("auth.not-registered"));
            case WRONG_PASSWORD -> ctx.reply(AuthMessages.raw("auth.wrong-current-password"));
            case TOO_SHORT -> ctx.reply(AuthMessages.format("auth.password-too-short",
                    Map.of("min", String.valueOf(AuthConfig.get().getMinPasswordLength()))));
            case REUSED -> ctx.reply(AuthMessages.raw("auth.password-reused"));
        }
    }
}
