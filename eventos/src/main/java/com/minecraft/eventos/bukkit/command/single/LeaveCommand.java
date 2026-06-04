package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.Core;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import org.bukkit.entity.Player;

public final class LeaveCommand implements CommandInheritor {

    @Command(name = "leave", aliases = {"sair", "hub"}, playerOnly = true,
            description = "Sai do evento e volta ao hub")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        String target = com.minecraft.eventos.EventosSettings.POST_END_TARGET;
        try {
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(target);
            player.sendPluginMessage(Core.getJavaPlugin(), "BungeeCord", b.toByteArray());
        } catch (Throwable t) {
            player.teleport(player.getWorld().getSpawnLocation());
        }
        ctx.reply("§eVoltando ao hub...");
    }
}
