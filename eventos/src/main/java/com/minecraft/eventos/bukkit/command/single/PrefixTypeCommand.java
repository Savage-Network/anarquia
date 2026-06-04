package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.account.context.objects.toggle.TagPrefix;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.annotation.Completer;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.bukkit.event.AccountPrefixTypeEvent;
import com.minecraft.eventos.bukkit.manager.TagManager;
import com.minecraft.eventos.util.AccountSync;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Espelho de {@code PrefixTypeCommand} (especificacao §3.7).
 *
 * <p>Sem cooldown. Dispara {@link AccountPrefixTypeEvent} para repintar tab.
 */
public final class PrefixTypeCommand implements CommandInheritor {

    @Command(name = "prefixtype", aliases = {"prefixo", "modotag"}, playerOnly = true,
            description = "Modo de exibicao das tags",
            usage = "/prefixtype [default|color|brackets|brackets_normal_case|default_white|default_gray]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length == 0) {
            TagManager.sendPrefixTypes(account);
            return;
        }

        TagPrefix prefix = TagPrefix.of(ctx.getArgs()[0]);
        if (prefix == null) { ctx.reply("§cModo de prefixo invalido."); return; }
        if (account.isUsingTagPrefix(prefix)) {
            ctx.reply("§eVoce ja usa o modo " + prefix.getDisplayName() + ".");
            return;
        }

        account.setTagPrefix(prefix);
        Bukkit.getPluginManager().callEvent(new AccountPrefixTypeEvent(account, prefix));
        AccountSync.saveAndBroadcast(account);
        ctx.reply("§aModo de prefixo: §e" + prefix.getDisplayName());
    }

    @Completer(name = "prefixtype")
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        for (TagPrefix p : TagPrefix.values()) {
            String name = p.name().toLowerCase();
            if (name.startsWith(partial)) out.add(name);
        }
        return out;
    }
}
