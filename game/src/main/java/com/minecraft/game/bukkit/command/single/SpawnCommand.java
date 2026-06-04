package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SpawnCommand implements CommandInheritor {

    @Command(name = "spawn", playerOnly = true, usage = "/spawn")
    public void spawn(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        Player player = ctx.player();

        if (CombatTagManager.isTagged(player)) {
            ctx.reply(CombatTagManager.blockedMessage(player));
            return;
        }

        if (!player.isOp() && account.hasCooldown("spawn-cmd")) {
            long left = account.getCooldownRemaining("spawn-cmd");
            ctx.reply("§cAguarde " + Math.max(1, left / 1000L) + "s.");
            return;
        }

        player.teleport(SemiAnarchyConfig.spawnPoint());
        if (!player.isOp()) {
            account.setCooldown("spawn-cmd", SemiAnarchyConfig.spawnCooldownMs());
        }
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.4, 0.5, 0.4, 0.05);
        ctx.reply("§aTeleportado para o spawn seguro.");
    }
}
