package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.LobbySettings.NetworkMode;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.network.QueueManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * /play [servidor] — entra na fila de um modo de jogo.
 * Sem args lista os modos disponíveis com clique para entrar.
 */
public final class PlayCommand implements CommandInheritor {

    @Command(name = "play", playerOnly = true,
            description = "Entra na fila de um modo de jogo",
            usage = "/play [modo]")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            sendModeList(ctx);
            return;
        }

        String input = ctx.getArgs()[0];
        NetworkMode mode = QueueManager.findMode(input);
        if (mode == null) {
            ctx.reply("§cModo §e" + input + " §cnão encontrado. Use §e/play §cpara ver os disponíveis.");
            return;
        }

        QueueManager.join(ctx.player(), mode.id());
    }

    @Completer(name = "play")
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        for (NetworkMode m : LobbySettings.NETWORK_MODES) {
            if (m.id().startsWith(partial) || m.bungeeServer().startsWith(partial)) {
                out.add(m.id());
            }
        }
        return out;
    }

    private void sendModeList(BukkitCommandContext ctx) {
        ctx.reply(Component.text(""));
        ctx.reply(Component.text("  Modos disponíveis:")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true));

        for (NetworkMode mode : LobbySettings.NETWORK_MODES) {
            String plain = mode.displayName().replaceAll("<[^>]+>", "");

            Component line = Component.text("  » ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(plain).color(NamedTextColor.WHITE))
                    .append(Component.text(" — clique para entrar")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, true))
                    .clickEvent(ClickEvent.runCommand("/play " + mode.id()))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Entrar na fila: " + plain).color(NamedTextColor.GREEN)));

            ctx.reply(line);
        }
        ctx.reply(Component.text(""));
    }
}
