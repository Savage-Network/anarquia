package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.annotation.Completer;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.game.Game;
import com.minecraft.eventos.game.team.TeamPresetType;
import com.minecraft.eventos.util.TabCompleteUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class TeamCommand implements CommandInheritor {

    @Command(name = "team", permission = "eventos.admin",
            description = "Ativa/distribui times",
            usage = "/team <on|off|distribute|select on|off>")
    public void execute(BukkitCommandContext ctx) {
        Game game = EventosPlugin.getInstance().getGame();
        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /team <on|off|distribute|select on|off>");
            return;
        }
        switch (ctx.getArgs()[0].toLowerCase()) {
            case "on" -> {
                game.setTeam(true);
                ctx.reply("§aModo Team ativado.");
            }
            case "off" -> {
                game.setTeam(false);
                game.getContext().getTeams().clear();
                ctx.reply("§eModo Team desativado.");
            }
            case "select" -> {
                boolean on = ctx.getArgs().length > 1 && ctx.getArgs()[1].equalsIgnoreCase("on");
                game.setTeamSelect(on);
                ctx.reply(on ? "§aSelecao de time aberta." : "§eSelecao de time fechada.");
            }
            case "distribute" -> {
                distribute(game);
                ctx.reply("§aJogadores distribuidos em §e" + countTeams(game) + " §atimes.");
            }
            default -> ctx.reply("§cSubcomando desconhecido.");
        }
    }

    @Completer(name = "team")
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], "on", "off", "distribute", "select");
        }
        if (ctx.getArgs().length == 2 && "select".equalsIgnoreCase(ctx.getArgs()[0])) {
            return TabCompleteUtil.filter(ctx.getArgs()[1], "on", "off");
        }
        return List.of();
    }

    private void distribute(Game game) {
        List<Player> players = new LinkedList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        TeamPresetType[] colors = TeamPresetType.values();
        int idx = 0;
        for (Player p : players) {
            TeamPresetType color = colors[idx % colors.length];
            game.getContext().getTeams().put(p.getUniqueId(), color);
            p.sendMessage(net.kyori.adventure.text.Component.text(
                    "§7Voce esta no time " + color.getColorCode() + "§l" + color.name()));
            idx++;
        }
    }

    private int countTeams(Game game) {
        return (int) game.getContext().getTeams().values().stream().distinct().count();
    }
}
