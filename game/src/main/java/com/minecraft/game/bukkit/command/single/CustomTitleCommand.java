package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.title.CustomTitle;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.display.AboveNameDisplay;

/**
 * Espelho de {@code CustomTitleCommand} do Savage. Rank minimo: ADMIN (Creator+
 * no Savage, mas em semi-anarquia usamos ADMIN+ para staff).
 *
 * <p>Maximo 32 caracteres visiveis, {@code &} traduzido para {@code §}.
 */
public final class CustomTitleCommand implements CommandInheritor {

    @Command(name = "customtitle", aliases = {"titulo"}, playerOnly = true,
            description = "Define titulo flutuante acima do nick",
            usage = "/customtitle <texto|remover>")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (!account.hasRank(RankType.ADMIN)) {
            ctx.reply("§cSomente Admin+ pode usar este comando.");
            return;
        }

        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /customtitle <texto|remover>");
            return;
        }

        if (ctx.getArgs()[0].equalsIgnoreCase("remover")
                || ctx.getArgs()[0].equalsIgnoreCase("remove")
                || ctx.getArgs()[0].equalsIgnoreCase("off")) {
            account.setCustomTitle(null);
            AboveNameDisplay.remove(ctx.player());
            ctx.reply("§eTitulo removido.");
            return;
        }

        String joined = String.join(" ", ctx.getArgs()).replace('&', '§');
        String visible = joined.replaceAll("§[0-9a-fk-or]", "");
        if (visible.length() > 32) {
            ctx.reply("§cTexto excede 32 caracteres visiveis.");
            return;
        }

        account.setCustomTitle(new CustomTitle(joined, "§d"));
        AboveNameDisplay.refresh(ctx.player());
        ctx.reply("§aTitulo definido: " + joined);
    }
}
