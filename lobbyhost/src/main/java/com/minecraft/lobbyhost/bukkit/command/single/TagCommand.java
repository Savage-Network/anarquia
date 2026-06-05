package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.event.AccountTagEvent;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.util.AccountSync;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Espelho de {@code TagCommand} do Savage. Lista hover sem args; troca com arg.
 * Cooldown de {@link Constant#TAG_CHANGE_COOLDOWN_MS} (staff isento).
 */
public final class TagCommand implements CommandInheritor {

    @Command(name = "tag", aliases = {"tags"}, playerOnly = true,
            description = "Lista/escolhe a sua tag", usage = "/tag [nome]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length == 0) {
            TagManager.sendTags(account);
            return;
        }

        Tag tag = Tag.of(ctx.getArgs()[0]);
        if (tag == null) {
            ctx.reply("§cTag desconhecida.");
            return;
        }
        if (!account.hasTag(tag)) {
            ctx.reply("§cVoce nao tem permissao para usar essa tag.");
            return;
        }
        if (account.isUsingTag(tag)) {
            ctx.reply("§eVoce ja esta usando " + tag.getColoredName() + "§e.");
            return;
        }
        if (!account.isStaffer() && account.hasCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY)) {
            long left = account.getCooldownRemaining(Constant.TAG_CHANGE_COOLDOWN_KEY);
            ctx.reply("§cAguarde " + Math.max(1, left / 1000L) + "s para trocar de tag novamente.");
            return;
        }

        account.setTag(tag);
        if (!account.isStaffer()) {
            account.setCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY, Constant.TAG_CHANGE_COOLDOWN_MS);
        }
        Bukkit.getPluginManager().callEvent(new AccountTagEvent(account, tag));
        AccountSync.saveAndBroadcast(account);
        ctx.reply("§aTag alterada para " + tag.getColoredName() + "§a.");
    }

    @Completer(name = "tag", subCommands = {"tags"})
    public List<String> tab(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        for (Tag t : account.getAvailableTags()) {
            String name = t.name().toLowerCase();
            if (name.startsWith(partial)) out.add(name);
        }
        return out;
    }
}
