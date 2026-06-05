package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Constant;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.welcome.WelcomeBookService;
import org.bukkit.entity.Player;

public final class LivroCommand implements CommandInheritor {

    @Command(name = "livro", playerOnly = true, usage = "/livro")
    public void livro(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }

        if (account.hasCooldown(Constant.LIVRO_COOLDOWN_KEY)) {
            long left = account.getCooldownRemaining(Constant.LIVRO_COOLDOWN_KEY);
            ctx.reply("§cAguarde §f" + formatCooldown(left) + " §cpara pegar outro livro.");
            return;
        }

        Player player = ctx.player();
        WelcomeBookService.deliver(player);
        account.setCooldown(Constant.LIVRO_COOLDOWN_KEY, Constant.LIVRO_COOLDOWN_MS);
        account.markDirty();
        ctx.reply("§6§lSavageMC §7» §aRecebeste o §fGuia do Jogador§a!");
    }

    private static String formatCooldown(long ms) {
        long totalSec = Math.max(1L, ms / 1000L);
        long hours = totalSec / 3600L;
        long minutes = (totalSec % 3600L) / 60L;
        if (hours > 0) {
            if (minutes > 0) return hours + "h " + minutes + "m";
            return hours + "h";
        }
        if (minutes > 0) return minutes + "m";
        return totalSec + "s";
    }
}
