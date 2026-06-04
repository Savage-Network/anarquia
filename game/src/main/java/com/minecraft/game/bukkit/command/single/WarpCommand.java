package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WarpCommand implements CommandInheritor {

    @Command(name = "warp", playerOnly = true, usage = "/warp <nome>")
    public void warp(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        Player player = ctx.player();

        if (ctx.getArgs().length == 0) {
            ctx.reply("§6Warps: §f" + String.join("§7, §f", SemiAnarchyConfig.warps().keySet()));
            return;
        }

        if (CombatTagManager.isTagged(player)) {
            ctx.reply(CombatTagManager.blockedMessage(player));
            return;
        }

        Location loc = SemiAnarchyConfig.warp(ctx.getArgs()[0]);
        if (loc == null) {
            ctx.reply("§cWarp nao encontrado.");
            return;
        }

        if (!player.isOp() && account.hasCooldown("warp-cmd")) {
            long left = account.getCooldownRemaining("warp-cmd");
            ctx.reply("§cAguarde " + Math.max(1, left / 1000L) + "s.");
            return;
        }

        player.teleport(loc);
        if (!player.isOp()) {
            account.setCooldown("warp-cmd", SemiAnarchyConfig.warpCooldownMs());
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 0.3, 0.4, 0.3, 0.05);
        ctx.reply("§aWarp §f" + ctx.getArgs()[0].toLowerCase(Locale.ROOT) + "§a.");
    }

    @Completer(name = "warp")
    public List<String> tab(BukkitCommandContext ctx) {
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase(Locale.ROOT) : "";
        List<String> out = new ArrayList<>();
        for (String w : SemiAnarchyConfig.warps().keySet()) {
            if (w.startsWith(partial)) out.add(w);
        }
        return out;
    }
}
