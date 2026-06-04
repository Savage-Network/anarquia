package com.minecraft.core.bungee.command.single;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.api.punishment.objects.enums.PunishmentReason;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bungee.bot.controller.BotController;
import com.minecraft.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.list.TimeUtil;
import net.md_5.bungee.api.chat.TextComponent;

public class CheatBanCommand implements CommandInheritor {

    @Command(name = "cban", rank = RankType.MOD, onlyPlayer = false)
    public void cban(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /" + context.getLabel() + " <nick> <tempo>");
            sender.send("§7Exemplos: §f/cban yatzo 7d§7, §f/cban yatzo 12h§7, §f/cban yatzo -1");
            return;
        }

        String targetName = args[0];
        String timeArg = args[1];

        Account target = context.getAccount(targetName);
        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (sender.getId().equals(target.getId())) {
            sender.send("§cVocê não pode banir a si mesmo.");
            return;
        }

        if (sender.isPlayer()) {
            Account actor = context.getAccount();
            if (!actor.hasRank(target.getRankType()) || actor.hasOnlyRank(target.getRankType())) {
                sender.send("§cVocê não pode punir alguém superior ou igual a você.");
                return;
            }
        }

        if (target.getActivePunishment(PunishmentCategory.BAN) != null) {
            sender.send("§cO jogador " + target.getName() + " já possui uma punição em aberto.");
            return;
        }

        long expiresAt = -1L;
        if (!timeArg.equalsIgnoreCase("-1") && !timeArg.equalsIgnoreCase("perm") && !timeArg.equalsIgnoreCase("perma")) {
            if (!TimeUtil.isValidTime(timeArg)) {
                sender.send("§cTempo inválido. Use algo como 10m, 2h, 7d ou -1.");
                return;
            }
            expiresAt = TimeUtil.getTime(timeArg);
        }

        Punishment punishment = Punishment.builder()
                .author(sender.getId())
                .player(target.getId())
                .server(target.getServerType())
                .arcade(target.getRoute().isValidArcade() ? target.getArcadeRoute().getArcade() : ArcadeCategory.NONE)
                .ip(target.getIpAddress())
                .reason(PunishmentReason.CHEATING)
                .category(PunishmentCategory.BAN)
                .cause("Cheating")
                .expiresAt(expiresAt)
                .build();

        Core.getPunishmentData().save(punishment);

        if (target.proxiedPlayer() != null) {
            target.proxiedPlayer().disconnect(TextComponent.fromLegacyText(String.format(
                    Constant.BAN_TEMPLATE_MESSAGE,
                    punishment.isTemporary() ? "temporariamente" : "permanentemente",
                    punishment.getReason().getName(),
                    punishment.getId()
            )));
        }

        sender.send("§cVocê baniu o jogador " + target.getName() + " por " + PunishmentReason.CHEATING.getName() + ".");

        BotController.sendPunishmentMessage(punishment);
    }
}

