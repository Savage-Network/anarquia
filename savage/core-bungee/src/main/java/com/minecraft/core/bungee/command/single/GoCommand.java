package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.redirect.Redirect;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.server.type.ServerType;

import java.util.List;

public class GoCommand implements CommandInheritor {

    @Completer(name = "go", rank = RankType.MOD)
    public List<String> goCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "go", rank = RankType.MOD)
    public void go(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " [jogador].");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline() || target.inServer(ServerType.AUTH)) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (target.inServer(account.getServerType())) {
            account.send("§cVocê já está no servidor de " + target.getNickname() + ".");
            return;
        }

        /* Redirecionar jogador */
        RouteContext route = RouteContext.copy(account.getId(), target.getRoute());

        if (route.isValidArcade())
            route.getArcade().setJoin(Join.VANISH);

        Core.getRedirectData().save(new Redirect(account.getId(), target.getId(), route));

        account.redirect(route);
    }
}
