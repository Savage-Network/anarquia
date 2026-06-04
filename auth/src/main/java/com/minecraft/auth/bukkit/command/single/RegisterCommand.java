package com.minecraft.auth.bukkit.command.single;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.command.annotation.Command;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.auth.bukkit.command.structure.CommandInheritor;
import com.minecraft.auth.service.AuthService;
import com.minecraft.auth.util.AuthRedirects;
import org.bukkit.entity.Player;

public final class RegisterCommand implements CommandInheritor {

    @Command(name = "register", aliases = {"registrar", "registar"}, playerOnly = true,
            description = "Registra uma nova conta",
            usage = "/register <senha> <confirmar>")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (AuthService.getInstance().isAuthenticated(player.getUniqueId())) {
            ctx.reply("§eVoce ja esta autenticado.");
            return;
        }

        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /register <senha> <confirmar>");
            return;
        }

        int min = com.minecraft.auth.AuthSettings.MIN_PASSWORD_LENGTH;
        AuthService.RegisterResult result = AuthService.getInstance()
                .register(account, ctx.getArgs()[0], ctx.getArgs()[1], min);

        switch (result) {
            case OK -> {
                ctx.reply("§aRegistro concluido. Bem-vindo, §e" + player.getName() + "§a!");
                AuthRedirects.toHub(player);
            }
            case ALREADY_REGISTERED -> ctx.reply("§cVoce ja esta registrado. Use §e/login <senha>§c.");
            case TOO_SHORT -> ctx.reply("§cSenha muito curta (minimo " + min + " caracteres).");
            case MISMATCH -> ctx.reply("§cAs senhas nao conferem.");
        }
    }

}
