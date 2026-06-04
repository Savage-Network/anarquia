package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public final class ClearMobsCommand implements CommandInheritor {

    @Command(name = "clearmobs", permission = "eventos.admin",
            description = "Remove todos os mobs do mundo")
    public void execute(BukkitCommandContext ctx) {
        int removed = 0;
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e instanceof Mob && !(e instanceof Player)) {
                    e.remove();
                    removed++;
                }
            }
        }
        ctx.reply("§a" + removed + " mob(s) removido(s).");
    }
}
