package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.home.StoredLocation;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.HomeService;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HomeCommand implements CommandInheritor {

    @Command(name = "sethome", playerOnly = true, usage = "/sethome [nome]")
    public void setHome(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        String name = ctx.getArgs().length > 0 ? ctx.getArgs()[0] : "home";
        ctx.reply(HomeService.setHome(account, ctx.player(), name));
    }

    @Command(name = "delhome", playerOnly = true, usage = "/delhome [nome]")
    public void delHome(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        String name = ctx.getArgs().length > 0 ? ctx.getArgs()[0] : "home";
        ctx.reply(HomeService.deleteHome(account, name));
    }

    @Command(name = "homes", playerOnly = true, usage = "/homes")
    public void homes(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        HomeService.sendList(account);
    }

    @Command(name = "home", playerOnly = true, usage = "/home [nome]")
    public void home(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        Player player = ctx.player();

        if (CombatTagManager.isTagged(player)) {
            ctx.reply(CombatTagManager.blockedMessage(player));
            return;
        }

        String name = ctx.getArgs().length > 0 ? ctx.getArgs()[0] : "home";
        StoredLocation stored = HomeService.getHome(account, name);
        if (stored == null) {
            ctx.reply("§cHome nao encontrada.");
            return;
        }

        if (!player.isOp() && account.hasCooldown("home-cmd")) {
            long left = account.getCooldownRemaining("home-cmd");
            ctx.reply("§cAguarde " + Math.max(1, left / 1000L) + "s.");
            return;
        }

        Location loc = stored.toBukkit();
        if (SpawnProtection.isProtected(loc)) {
            ctx.reply("§cEsta home esta no spawn protegido.");
            return;
        }

        player.teleport(loc);
        if (!player.isOp()) {
            account.setCooldown("home-cmd", SemiAnarchyConfig.homeCooldownMs(account.isVIP()));
        }
        account.markDirty();
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 25, 0.4, 0.5, 0.4, 0.05);
        ctx.reply("§aTeleportado para home §f" + HomeService.normalize(name) + "§a.");
    }

    @Completer(name = "home")
    public List<String> tabHome(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) return List.of();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase(Locale.ROOT) : "";
        List<String> out = new ArrayList<>();
        for (String h : HomeService.listHomes(account)) {
            if (h.startsWith(partial)) out.add(h);
        }
        return out;
    }

    @Completer(name = "delhome")
    public List<String> tabDel(BukkitCommandContext ctx) {
        return tabHome(ctx);
    }
}
