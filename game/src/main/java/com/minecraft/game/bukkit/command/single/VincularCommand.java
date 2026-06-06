package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.DiscordLinkData;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.config.DiscordConfig;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class VincularCommand implements CommandInheritor {

    @Command(name = "vincular", aliases = {"link", "vinculo", "discordlink"},
            playerOnly = true,
            description = "Gera um codigo para vincular sua conta ao Discord",
            usage = "/vincular")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }

        DiscordLinkData data = Core.getDiscordLinkData();
        if (data == null) {
            ctx.replyKey("discord.link.unavailable");
            return;
        }

        Player player = ctx.player();
        var lang = LanguageService.of(player);

        if (data.findLinkByPlayer(account.getId()).isPresent()) {
            player.sendMessage(Messages.component("discord.link.already-linked", lang));
            return;
        }

        Core.getJavaPlugin().getServer().getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            try {
                String code = data.createCode(account.getId(), account.getName());
                long minutes = TimeUnit.MILLISECONDS.toMinutes(data.codeTtlMs());
                String invite = DiscordConfig.inviteDisplay();

                Core.getJavaPlugin().getServer().getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (!player.isOnline()) return;
                    player.sendMessage(Messages.component("discord.link.code-header", lang));
                    player.sendMessage(Messages.component("discord.link.code-line", lang, Map.of(
                            "code", code,
                            "minutes", String.valueOf(minutes))));
                    player.sendMessage(Messages.component("discord.link.code-hint", lang, Map.of(
                            "code", code,
                            "invite", invite)));
                });
            } catch (RuntimeException e) {
                Core.getJavaPlugin().getServer().getScheduler().runTask(Core.getJavaPlugin(), () -> {
                    if (!player.isOnline()) return;
                    player.sendMessage(Messages.component("discord.link.error", lang));
                });
                Core.getLogger().severe("Erro ao gerar codigo Discord: " + e.getMessage());
            }
        });
    }
}
