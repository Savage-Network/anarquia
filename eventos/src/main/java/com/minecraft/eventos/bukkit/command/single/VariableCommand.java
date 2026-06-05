package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.annotation.Completer;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.game.variable.Variable;
import com.minecraft.eventos.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;

public final class VariableCommand implements CommandInheritor {

    @Command(name = "variable", aliases = {"var"}, permission = "eventos.admin",
            description = "Get/set variaveis globais do evento",
            usage = "/variable <list|get|set|reset> [chave] [valor]")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /variable <list|get|set|reset> [chave] [valor]");
            return;
        }
        switch (ctx.getArgs()[0].toLowerCase()) {
            case "list" -> {
                ctx.reply("§eVariaveis:");
                Variable.getValues().forEach((k, v) ->
                        ctx.reply("§7- §e" + k.name() + " §7= §f" + v));
            }
            case "get" -> {
                if (ctx.getArgs().length < 2) { ctx.reply("§cUso: /variable get <chave>"); return; }
                Variable.Key key = Variable.Key.of(ctx.getArgs()[1]);
                if (key == null) { ctx.reply("§cChave invalida."); return; }
                ctx.reply("§e" + key.name() + " §7= §f" + Variable.get(key));
            }
            case "set" -> {
                if (ctx.getArgs().length < 3) { ctx.reply("§cUso: /variable set <chave> <valor>"); return; }
                Variable.Key key = Variable.Key.of(ctx.getArgs()[1]);
                if (key == null) { ctx.reply("§cChave invalida."); return; }
                Object parsed = parse(ctx.getArgs()[2], key.getDefaultValue());
                Variable.set(key, parsed);
                ctx.reply("§a" + key.name() + " §7= §f" + parsed);
            }
            case "reset" -> {
                if (ctx.getArgs().length < 2 || "all".equalsIgnoreCase(ctx.getArgs()[1])) {
                    Variable.resetAll();
                    ctx.reply("§aTodas as variaveis foram resetadas.");
                } else {
                    Variable.Key key = Variable.Key.of(ctx.getArgs()[1]);
                    if (key == null) { ctx.reply("§cChave invalida."); return; }
                    Variable.reset(key);
                    ctx.reply("§a" + key.name() + " resetada para §f" + key.getDefaultValue());
                }
            }
            default -> ctx.reply("§cSubcomando desconhecido.");
        }
    }

    @Completer(name = "variable", subCommands = {"var"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], "list", "get", "set", "reset");
        }
        if (ctx.getArgs().length == 2) {
            String sub = ctx.getArgs()[0].toLowerCase();
            if (sub.equals("get") || sub.equals("set") || sub.equals("reset")) {
                List<String> keys = new ArrayList<>(List.of("all"));
                for (Variable.Key key : Variable.Key.values()) keys.add(key.name().toLowerCase());
                return TabCompleteUtil.filter(ctx.getArgs()[1], keys);
            }
        }
        return List.of();
    }

    private Object parse(String raw, Object hint) {
        if (hint instanceof Boolean) return Boolean.parseBoolean(raw);
        if (hint instanceof Integer) { try { return Integer.parseInt(raw); } catch (Throwable t) { return raw; } }
        if (hint instanceof Double) { try { return Double.parseDouble(raw); } catch (Throwable t) { return raw; } }
        return raw;
    }
}
