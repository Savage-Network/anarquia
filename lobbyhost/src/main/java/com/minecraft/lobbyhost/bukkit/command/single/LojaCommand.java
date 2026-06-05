package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.event.ClickEvent;

public final class LojaCommand implements CommandInheritor {

    @Command(name = "loja", aliases = {"store", "shop", "comprar"}, playerOnly = true,
            description = "Mostra o link da loja")
    public void execute(BukkitCommandContext ctx) {
        ctx.reply(ModernText.parse(
            "<!italic>\n"
          + "<dark_gray>  ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"
          + "  <gradient:#FF8C42:#FFD166><bold>LOJA SAVAGE</bold></gradient>\n"
          + "  <gray>Adquira ranks, cosméticos e muito mais!\n"
          + "\n"
          + "  <yellow><underlined><click:open_url:'https://" + LobbySettings.SERVER_STORE_URL + "'>"
          + LobbySettings.SERVER_STORE_URL + "</click></underlined>\n"
          + "<dark_gray>  ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"
        ));
    }
}
