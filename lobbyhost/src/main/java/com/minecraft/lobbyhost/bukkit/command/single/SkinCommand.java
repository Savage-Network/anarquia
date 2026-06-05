package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import com.minecraft.lobbyhost.util.TabCompleteUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /skin — abre a biblioteca de skins (sem args) ou copia a skin de outro jogador (/skin <nick>).
 */
public final class SkinCommand implements CommandInheritor {

    @Command(name = "skin", aliases = {"skinchanger", "updateskin"}, playerOnly = true,
            description = "Abre a biblioteca de skins ou copia a de outro jogador", usage = "/skin [nick]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }


        // Com args → copia skin de outro nick (VIP+)
        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: §f/skin <nick> §7— copia a skin de outro jogador");
            return;
        }
        if (!account.isVIP() && !account.isStaffer()) {
            ctx.reply("§cApenas VIP ou superior pode copiar skins de outros jogadores.");
            return;
        }

        Player player = ctx.player();
        String source = ctx.getArgs()[0];
        if (source.isBlank()) {
            ctx.reply("§cNick inválido.");
            return;
        }

        if (!account.isStaffer() && account.hasCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY)) {
            long left = account.getCooldownRemaining(Constant.SKIN_CHANGE_COOLDOWN_KEY);
            ctx.reply("§cAguarde " + Math.max(1, left / 1000L) + "s para trocar de skin novamente.");
            return;
        }

        ctx.reply("§eBuscando skin de §f" + source + "§e...");
        SkinService.applyMojangSkin(account, player, source,
                () -> {
                    if (!account.isStaffer()) {
                        account.setCooldown(Constant.SKIN_CHANGE_COOLDOWN_KEY, Constant.SKIN_CHANGE_COOLDOWN_MS);
                    }
                    ctx.reply("§aSkin de §f" + source + " §aaplicada com sucesso.");
                },
                () -> ctx.reply("§cNao foi possivel obter a skin de §f" + source + "§c."));
    }

    @Completer(name = "skin", subCommands = {"skinchanger", "updateskin"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        }
        return List.of();
    }
}
