package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.social.PartyService;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PartyCommand implements CommandInheritor {

    @Command(name = "party", aliases = {"p", "grupo"}, playerOnly = true,
            description = "Party leve do lobby",
            usage = "/party [criar|convidar|sair|info] [nick]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }
        Player player = ctx.player();
        String[] args = ctx.getArgs();
        if (args.length == 0) {
            sendInfo(player);
            ctx.reply("§7/party criar | convidar | sair");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "criar", "create" -> PartyService.create(player);
            case "convidar", "invite" -> {
                if (args.length < 2) {
                    ctx.reply("§eUso: /party convidar <nick>");
                    return;
                }
                PartyService.invite(account, args[1]);
            }
            case "sair", "leave" -> PartyService.leave(player);
            case "info" -> sendInfo(player);
            default -> ctx.reply("§e/party criar|convidar|sair|info");
        }
    }

    private void sendInfo(Player player) {
        UUID leader = PartyService.leaderOf(player.getUniqueId());
        if (leader == null) {
            player.sendMessage(net.kyori.adventure.text.Component.text("§7Voce nao esta em uma party."));
            return;
        }
        var members = PartyService.members(leader);
        player.sendMessage(net.kyori.adventure.text.Component.text(
                "§6Party §7(" + (members.size() + 1) + " jogadores) §8- lider: "
                        + (leader.equals(player.getUniqueId()) ? "voce" : BukkitOfflineName(leader))));
    }

    private static String BukkitOfflineName(UUID id) {
        var off = org.bukkit.Bukkit.getOfflinePlayer(id);
        return off.getName() != null ? off.getName() : id.toString().substring(0, 8);
    }
}
