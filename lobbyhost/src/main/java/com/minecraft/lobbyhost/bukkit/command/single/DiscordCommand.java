package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.config.DiscordConfig;
import com.minecraft.lobbyhost.util.ModernText;

public final class DiscordCommand implements CommandInheritor {

    @Command(name = "discord", aliases = {"dc"}, playerOnly = true,
            description = "Mostra o link do Discord")
    public void execute(BukkitCommandContext ctx) {
        if (!DiscordConfig.isEnabled() || !DiscordConfig.feature("invite-command")) {
            ctx.reply("§cDiscord indisponível no momento.");
            return;
        }

        String title = DiscordConfig.message("command-title");
        if (title.isBlank()) title = "DISCORD SAVAGE";
        String invite = DiscordConfig.inviteDisplay();
        String url = DiscordConfig.inviteUrl();

        ctx.reply(ModernText.parse(
            "<!italic>\n"
          + "<dark_gray>  ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"
          + "  <gradient:#C084FC:#818CF8><bold>" + title + "</bold></gradient>\n"
          + "  <gray>Entre na nossa comunidade e fique por dentro!\n"
          + "\n"
          + "  <aqua><underlined><click:open_url:'" + url + "'>"
          + invite + "</click></underlined>\n"
          + "<dark_gray>  ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"
        ));
    }
}
