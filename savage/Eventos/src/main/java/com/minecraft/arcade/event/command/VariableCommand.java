package com.minecraft.arcade.event.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.arcade.event.game.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VariableCommand implements CommandInheritor {

    @Completer(
            name = "variable",
            subCommands = {"var"},
            rank = RankType.ADMIN
    )
    public List<String> variableCompleter(BukkitCommandContext context) {
        String[] args = context.getArgs();

        List<String> result = new ArrayList<>(),
                names = Variable.find().stream().map(Variable::getName).toList();

        if (args.length > 0 && !args[0].isEmpty()) {
            String varName = args[0].toLowerCase();

            for (String name : names) {
                if (name.toLowerCase().startsWith(varName))
                    result.add(name);
            }

        } else
            result.addAll(names);

        return result;
    }

    @Command(
            name = "variable",
            aliases = {"var"},
            rank = RankType.ADMIN,
            onlyPlayer = false
    )
    public void variable(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        Set<Variable> variables = Variable.find();

        if (args.length == 0) {
            sender.send("§cUso do /" + context.getLabel() + ":");

            variables.forEach(variable -> sender.send("§c* /%s %s <valor>"
                    .formatted(context.getLabel(), variable.getName()))
            );

            return;
        }

        if (args[0].equalsIgnoreCase("list")) {

            sender.send("",
                    "§aLista de variáveis:");

            variables.forEach(var -> sender.send("§e%s §6= §b%s"
                    .formatted(var.getName(), var.getValue())));

            return;
        }

        Variable var = Variable.fromName(args[0]);

        if (var == null) {
            sender.send("§cA variável selecionada não foi encontrada.");
            return;
        }

        if (args.length == 1) {
            sender.send("§cUso: /" + context.getLabel() + " " + var.getName() + " <valor>.");
            return;
        }

        String search = var.isBigArgs() ? context.getMessage(1, args) : args[1];

        Object parsed = var.parseValue(search);

        if (parsed == null) {
            sender.send("§cO valor é inválido para a variável '" + var.getName() + "'.");
            return;
        }

        if (var.isValue(parsed)) {
            sender.send("§cO valor '" + parsed + "' já foi aplicado.");
            return;
        }

        var.setValue(parsed);

        sender.send("§aVocê alterou a variável '%s' para '%s§a'.".formatted(var.getName(), var.getValue()));
    }
}
