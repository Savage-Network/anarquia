package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.toggle.Toggle;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.event.AccountTagEvent;
import org.bukkit.Bukkit;

/**
 * Substituto textual do {@code PreferenceMenu} do Savage. Como a GUI completa
 * exige inventario chest custom, oferecemos um set de subcomandos rapidos.
 */
public final class PrefsCommand implements CommandInheritor {

    @Command(name = "prefs", aliases = {"preferencias"}, playerOnly = true,
            description = "Lista/altera preferencias da conta",
            usage = "/prefs [list|toggle <chave>]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length == 0 || ctx.getArgs()[0].equalsIgnoreCase("list")) {
            list(account);
            return;
        }
        if (ctx.getArgs()[0].equalsIgnoreCase("toggle") && ctx.getArgs().length >= 2) {
            toggle(account, ctx.getArgs()[1]);
            ctx.reply("§aPreferencia atualizada.");
            return;
        }
        ctx.reply("§eUso: /prefs [list|toggle <chave>]");
    }

    private void list(Account account) {
        Toggle t = account.getToggle();
        account.send("§6§lPreferencias");
        account.send(line("mensagens",        t.isAllowPrivateMessages()));
        account.send(line("amizades",         t.isAllowFriendRequests()));
        account.send(line("party",            t.isAllowPartyInvites()));
        account.send(line("cla",              t.isAllowClanInvites()));
        account.send(line("clantag",          t.isShowClanTag()));
        account.send(line("stats",            t.isShowStats()));
        account.send(line("joinmessage",      t.isAllowJoinMessage()));
        if (account.isStaffer()) {
            account.send(line("staffchat",    t.isStaffChat()));
            account.send(line("autovanish",   t.isAutoVanish()));
        }
        account.send("§7Use §e/prefs toggle <chave>§7 para alternar.");
    }

    private String line(String key, boolean on) {
        return "§7- §f" + key + "§7: " + (on ? "§aativado" : "§cdesativado");
    }

    private void toggle(Account account, String rawKey) {
        Toggle t = account.getToggle();
        switch (rawKey.toLowerCase()) {
            case "mensagens", "messages"     -> t.setAllowPrivateMessages(!t.isAllowPrivateMessages());
            case "amizades", "friends"       -> t.setAllowFriendRequests(!t.isAllowFriendRequests());
            case "party"                     -> t.setAllowPartyInvites(!t.isAllowPartyInvites());
            case "cla", "clan"               -> t.setAllowClanInvites(!t.isAllowClanInvites());
            case "clantag" -> {
                t.setShowClanTag(!t.isShowClanTag());
                // Trigger update tab
                Bukkit.getPluginManager().callEvent(new AccountTagEvent(account, account.getTag()));
            }
            case "stats"                     -> t.setShowStats(!t.isShowStats());
            case "joinmessage"               -> t.setAllowJoinMessage(!t.isAllowJoinMessage());
            case "staffchat"                 -> { if (account.isStaffer()) t.setStaffChat(!t.isStaffChat()); }
            case "autovanish"                -> { if (account.isStaffer()) t.setAutoVanish(!t.isAutoVanish()); }
            default -> { account.send("§cChave desconhecida."); return; }
        }
        account.markDirty();
        list(account);
    }
}
