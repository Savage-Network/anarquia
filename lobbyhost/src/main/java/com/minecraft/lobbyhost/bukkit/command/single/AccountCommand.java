package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /account [jogador]} - imprime os campos do {@link com.minecraft.lobbyhost.account.context.AccountContext}.
 * Serve como teste end-to-end da Fase 1 (load -> cache -> render).
 */
public final class AccountCommand implements CommandInheritor {

    @Command(name = "account", aliases = {"perfil", "conta"},
            description = "Mostra os dados da conta", usage = "/account [jogador]")
    public void execute(BukkitCommandContext ctx) {
        Account target;

        if (ctx.getArgs().length == 0) {
            if (!ctx.isPlayer()) {
                ctx.reply("§cUso: /account <jogador>");
                return;
            }
            target = Core.getAccountController().of(ctx.player().getUniqueId());
        } else {
            String name = ctx.getArgs()[0];
            OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            target = Core.getAccountController().of(op.getUniqueId());
            if (target == null) {
                ctx.reply("§cJogador §e" + name + " §cnao esta online ou nao possui conta carregada.");
                return;
            }
        }

        Tag tag = target.getTag() != null ? target.getTag() : Tag.MEMBER;
        Medal medal = target.getMedal() != null ? target.getMedal() : Medal.NONE;

        ctx.reply("§6§l━━━━━ §eConta: §f" + target.getName() + " §6§l━━━━━");
        ctx.reply("§eUUID: §f" + target.getId());
        ctx.reply("§eRank: §f" + target.getRankType());
        ctx.reply("§eTag: " + tag.getColor() + tag.getDisplayName());
        ctx.reply("§eMedalha: §f" + medal.name() + " " + medal.getSymbol());
        ctx.reply("§eGolds: §6" + target.getGolds() + "§7  Level: §f" + target.getLevel());
        ctx.reply("§eIP: §f" + target.getIpAddress());
        ctx.reply("§ePremium: §f" + target.isPremium());
        ctx.reply("§eCosmeticos ativos: §f" + target.getActiveCollectibles().size());
        ctx.reply("§eFavoritos: §f" + target.getFavoriteCollectibles().size());
        ctx.reply("§eDirty (cache): §f" + target.isDirty());
        ctx.reply("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Completer(name = "account")
    public List<String> complete(BukkitCommandContext ctx) {
        List<String> names = new ArrayList<>();
        if (ctx.getArgs().length == 1) {
            String prefix = ctx.getArgs()[0].toLowerCase();
            for (Account account : Core.getAccountController().list()) {
                if (account.getName().toLowerCase().startsWith(prefix)) {
                    names.add(account.getName());
                }
            }
        }
        return names;
    }
}
