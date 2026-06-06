package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Constant;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.skin.SkinService;
import com.minecraft.game.util.TabCompleteUtil;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Atualiza a skin da conta a partir da Mojang ({@code /skin}) ou de outro nick ({@code /skin <nick>}).
 */
public final class SkinCommand implements CommandInheritor {

    @Command(name = "skin", aliases = {"skinchanger", "updateskin"}, playerOnly = true,
            description = "Atualiza sua skin ou copia a de outro jogador", usage = "/skin [nick]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        if (!account.isVIP() && !account.isStaffer()) {
            ctx.reply("§cApenas VIP ou superior pode alterar a skin.");
            return;
        }

        Player player = ctx.player();
        String source = ctx.getArgs().length > 0 ? ctx.getArgs()[0] : account.getName();
        if (source == null || source.isBlank()) {
            ctx.reply("§cNick invalido para buscar skin.");
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
                    ctx.reply("§aSkin atualizada com sucesso.");
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
