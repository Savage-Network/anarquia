package com.minecraft.auth.bukkit.command.single;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.command.annotation.Command;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.auth.bukkit.command.structure.CommandInheritor;
import com.minecraft.auth.service.AuthService;

public final class ChangePasswordCommand implements CommandInheritor {

    @Command(name = "changepassword", aliases = {"trocarsenha"}, playerOnly = true,
            description = "Altera a senha da sua conta",
            usage = "/changepassword <senha_atual> <nova_senha>")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /changepassword <senha_atual> <nova_senha>");
            return;
        }

        int min = com.minecraft.auth.AuthSettings.MIN_PASSWORD_LENGTH;
        AuthService.ChangePasswordResult result = AuthService.getInstance()
                .changePassword(account, ctx.getArgs()[0], ctx.getArgs()[1], min);

        switch (result) {
            case OK -> ctx.reply("§aSenha alterada com sucesso.");
            case NOT_REGISTERED -> ctx.reply("§cVoce ainda nao registrou.");
            case WRONG_PASSWORD -> ctx.reply("§cSenha atual incorreta.");
            case TOO_SHORT -> ctx.reply("§cSenha muito curta (minimo " + min + " caracteres).");
            case REUSED -> ctx.reply("§cVoce nao pode reutilizar uma senha antiga.");
        }
    }
}
