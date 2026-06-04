package com.minecraft.arcade.skywars.command;

import com.minecraft.arcade.skywars.user.User;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandInheritor {

    @Command(name = "sw")
    public void chat(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) {
            player.sendMessage("§cVocê não está em uma partida de SkyWars!");
            return;
        }

        Arena arena = user.getArena();

        if (arena == null) {
            player.sendMessage("§cVocê não está em uma arena!");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /sw <mensagem>");
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String chatMessage = message.toString().trim();

        // Enviar mensagem para todos na arena
        arena.getUsers().forEach(target -> {
            target.getAccount().send("§b[SW] §f" + player.getName() + ": §7" + chatMessage);
        });
    }
}

