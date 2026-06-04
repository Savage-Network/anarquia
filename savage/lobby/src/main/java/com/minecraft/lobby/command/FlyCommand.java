package com.minecraft.lobby.command;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandInheritor {

    @Command(name = "fly", aliases = {"voar"}, rank = RankType.VIP)
    public void fly(BukkitCommandContext context) {
        Player player = context.getPlayer();

        player.setFlying(!player.isFlying());
        player.sendMessage(player.isFlying() ? "§aVocê ativou o seu modo de voo." : "§cVocê desativou o seu modo de voo.");
    }
}
