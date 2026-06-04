package com.minecraft.auth.bukkit.command.single;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.command.annotation.Command;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.auth.bukkit.command.structure.CommandInheritor;
import com.minecraft.auth.service.AuthService;
import com.minecraft.auth.util.AuthRedirects;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class LoginCommand implements CommandInheritor {

    @Command(name = "login", aliases = {"logar", "entrar"}, playerOnly = true,
            description = "Faz login na sua conta", usage = "/login <senha>")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }

        if (AuthService.getInstance().isAuthenticated(player.getUniqueId())) {
            ctx.reply("§eVoce ja esta autenticado.");
            return;
        }

        if (ctx.getArgs().length < 1) {
            ctx.reply("§cUso: /login <senha>");
            return;
        }

        String password = ctx.getArgs()[0];
        AuthService.LoginResult result = AuthService.getInstance().login(account, password);

        switch (result) {
            case OK -> {
                ctx.reply("§aLogin efetuado com sucesso.");
                AuthRedirects.toHub(player);
            }
            case NOT_REGISTERED ->
                    ctx.reply("§cVoce ainda nao registrou. Use §e/register <senha> <confirmar>§c.");
            case WRONG_PASSWORD -> ctx.reply("§cSenha incorreta. Tentativas restantes: §e"
                    + AuthService.getInstance().getAttemptsLeft(player.getUniqueId()));
            case KICK -> player.kick(Component.text("§cTentativas excedidas."));
        }
    }

}
