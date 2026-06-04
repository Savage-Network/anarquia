package com.minecraft.arcade.event.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.controller.SkitController;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.skit.Skit;
import com.minecraft.arcade.event.game.skit.menu.SkitMenu;
import com.minecraft.arcade.event.game.skit.menu.info.SkitInfoMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SkitCommand implements CommandInheritor {

    @Command(
            name = "skit",
            rank = RankType.ADMIN
    )
    public void skit(BukkitCommandContext context) {

        Player player = context.getPlayer();

        String[] args = context.getArgs();

        String label = "/" + context.getLabel();

        if (args.length == 0) {
            sendHelpMessage(player, label);
            return;
        }

        Game game = Event.getGame();

        PlayerInventory inventory = player.getInventory();

        switch (args[0].toLowerCase()) {

            case "create" -> {

                if (args.length == 1) {
                    player.sendMessage("§cUso: " + label + " create <name>.");
                    return;
                }

                String name = args[1];

                if (SkitController.has(name)) {
                    player.sendMessage("§cUm kit com o nome '" + name + "' já existe.");
                    return;
                }

                if (BukkitUtil.isInventoryEmpty(player)) {
                    player.sendMessage("§cVocê não pode criar um kit com o inventário vázio.");
                    return;
                }

                String data = Serialization.serializeInventoryOfPlayer(player);

                SkitController.save(
                        new Skit(name, player.getUniqueId(), data)
                );

                player.sendMessage("§aO kit '" + name + "' foi criado com sucesso.");
            }

            case "remove" -> {

                if (args.length == 1) {
                    player.sendMessage("§cUso: " + label + " remove <name>.");
                    return;
                }

                String name = args[1];

                if (!SkitController.has(name)) {
                    player.sendMessage("§cO kit '" + name + "' não foi encontrado.");
                    return;
                }

                SkitController.remove(name);

                player.sendMessage("§cO kit '" + name + "' foi removido com sucesso.");
            }

            case "update" -> {

                if (args.length == 1) {
                    player.sendMessage("§cUso: " + label + " update <name>.");
                    return;
                }

                String name = args[1];

                if (!SkitController.has(name)) {
                    player.sendMessage("§cO kit '" + name + "' não foi encontrado.");
                    return;
                }

                if (BukkitUtil.isInventoryEmpty(player)) {
                    player.sendMessage("§cVocê não pode atualizar um kit com o inventário vázio.");
                    return;
                }

                Skit skit = SkitController.of(name);

                String data = Serialization.serializeInventoryOfPlayer(player);

                skit.setData(data);

                player.sendMessage("§aO kit '" + name + "' foi atualizado com sucesso.");
            }

            case "view" -> {

                if (args.length == 1) {
                    player.sendMessage("§cUso: " + label + " view <name>.");
                    return;
                }

                String name = args[1];

                if (!SkitController.has(name)) {
                    player.sendMessage("§cO kit '" + name + "' não foi encontrado.");
                    return;
                }

                Skit skit = SkitController.of(name);

                new SkitInfoMenu(player, skit, null).handle();
            }

            case "apply" -> {

                if (args.length <= 2) {
                    player.sendMessage("§cUso: " + label + " apply <name> <player | all>.");
                    return;
                }

                String name = args[1];

                if (!SkitController.has(name)) {
                    player.sendMessage("§cO kit '" + name + "' não foi encontrado.");
                    return;
                }

                Skit skit = SkitController.of(name);

                String playerName = args[2];

                if (playerName.equalsIgnoreCase("all")) {

                    // Dar o kit para todos os jogadores

                    game.getUsers().forEach(user -> {
                        Player target = org.bukkit.Bukkit.getPlayer(user.getAccount().getId());

                        if (target != null)
                            skit.apply(target);
                    });

                } else {

                    Player target = context.getPlayer(playerName);

                    if (target == null) {
                        player.sendMessage(TARGET_NOT_FOUND);
                        return;
                    }

                    skit.apply(target);
                }
            }

            case "list" -> new SkitMenu(player).handle();

            default -> sendHelpMessage(player, label);
        }
    }

    protected void sendHelpMessage(Player player, String label) {
        player.sendMessage(new String[]{
                "§cUso do " + label + ":",
                "§c* " + label + " create <name>",
                "§c* " + label + " remove <name>",
                "§c* " + label + " update <name>",
                "§c* " + label + " view <name>",
                "§c* " + label + " apply <name> <player | all>",
                "§c* " + label + " list"
        });
    }
}
