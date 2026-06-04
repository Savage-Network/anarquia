package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.event.AccountMedalEvent;
import com.minecraft.lobbyhost.bukkit.manager.MedalManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Espelho de {@code MedalCommand} do Savage. Sem cooldown.
 */
public final class MedalCommand implements CommandInheritor {

    @Command(name = "medal", aliases = {"medalha", "medalhas"}, playerOnly = true,
            description = "Lista/escolhe a sua medalha", usage = "/medal [nome]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length == 0) {
            MedalManager.sendMedals(account);
            return;
        }

        Medal medal = Medal.of(ctx.getArgs()[0]);
        if (medal == null) { ctx.reply("§cMedalha desconhecida."); return; }
        if (!account.hasMedal(medal)) { ctx.reply("§cVoce nao possui essa medalha."); return; }
        if (account.isUsingMedal(medal)) {
            ctx.reply("§eVoce ja esta usando " + medal.getColoredName() + "§e.");
            return;
        }

        account.setMedal(medal);
        Bukkit.getPluginManager().callEvent(new AccountMedalEvent(account, medal));
        ctx.reply(medal == Medal.NONE
                ? "§eMedalha removida."
                : "§aMedalha alterada para " + medal.getColoredName() + "§a.");
    }

    @Completer(name = "medal")
    public List<String> tab(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        for (Medal m : account.getAvailableMedals()) {
            String name = m.name().toLowerCase();
            if (name.startsWith(partial)) out.add(name);
        }
        return out;
    }
}
