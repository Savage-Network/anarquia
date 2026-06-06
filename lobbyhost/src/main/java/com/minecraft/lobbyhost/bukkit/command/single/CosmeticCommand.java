package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.ParticleEffect;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.cosmetic.CapeParticleRegistry;
import com.minecraft.lobbyhost.bukkit.cosmetic.CosmeticParticleRegistry;
import com.minecraft.lobbyhost.bukkit.menu.CosmeticMenus;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CosmeticCommand implements CommandInheritor {

    @Command(name = "cosmetic", aliases = {"cosmeticos", "cosmetico", "cos"}, playerOnly = true,
            description = "Hub de cosméticos (capas e auras)", usage = "/cosmetic [sub] [id]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }

        if (ctx.getArgs().length == 0) {
            CosmeticMenus.openMainMenu(ctx.player());
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase();
        Player player = ctx.player();

        if ("off".equals(sub) || "desativar".equals(sub)) {
            account.setActiveCape(null);
            account.setActiveParticle(null);
            CapeParticleRegistry.clear(player.getUniqueId());
            CosmeticParticleRegistry.clear(player.getUniqueId());
            ctx.reply("§aCosméticos desativados!");
            return;
        }

        if ("hub".equals(sub) || "menu".equals(sub)) {
            CosmeticMenus.openMainMenu(player);
            return;
        }
        if ("cape".equals(sub) || "capa".equals(sub) || "capas".equals(sub)) {
            if (ctx.getArgs().length < 2) { CosmeticMenus.openCapeMenu(player); return; }
            equipCape(ctx, account, player, ctx.getArgs()[1]);
            return;
        }
        if ("particle".equals(sub) || "particula".equals(sub) || "aura".equals(sub)) {
            if (ctx.getArgs().length < 2) { CosmeticMenus.openParticleMenu(player); return; }
            equipParticle(ctx, account, ctx.getArgs()[1]);
            return;
        }
        if ("ember".equals(sub) || "embercor".equals(sub)) {
            CosmeticMenus.openEmberColorMenu(player);
            return;
        }
        if ("unlock".equals(sub) && player.hasPermission("lobbyhost.admin")) {
            if (ctx.getArgs().length < 3) {
                ctx.reply("§cUso: /cosmetic unlock <tipo> <id>");
                return;
            }
            String type = ctx.getArgs()[1].toLowerCase();
            String id = ctx.getArgs()[2].toLowerCase();
            account.unlockCosmetic(type + ":" + id);
            ctx.reply("§aDesbloqueado: §f" + type + ":" + id);
            return;
        }

        ctx.reply("§cSubcomandos: hub, cape, particle, ember, off");
    }

    private void equipCape(BukkitCommandContext ctx, Account account, Player player, String name) {
        Cape cape = Cape.fromId(name);
        if (cape == null) { ctx.reply("§cCapa desconhecida."); return; }
        if (!account.hasCape(cape)) { ctx.reply("§cVocê não possui essa capa!"); return; }
        account.setActiveCape(cape);
        ctx.reply("§aAsas " + cape.getDisplayName() + " §aequipadas!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
    }

    private void equipParticle(BukkitCommandContext ctx, Account account, String name) {
        ParticleEffect effect = ParticleEffect.fromId(name);
        if (effect == null) { ctx.reply("§cPartícula desconhecida."); return; }
        if (!account.hasParticle(effect)) { ctx.reply("§cVocê não possui essa aura!"); return; }
        account.setActiveParticle(effect);
        ctx.reply("§aAura " + effect.getDisplayName() + " §aativada!");
    }

    @Completer(name = "cosmetic")
    public List<String> tab(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) return new ArrayList<>();

        List<String> out = new ArrayList<>();
        String[] args = ctx.getArgs();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String sub : Arrays.asList("hub", "cape", "particle", "ember", "off", "unlock")) {
                if (sub.startsWith(partial)) out.add(sub);
            }
            return out;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String partial = args[1].toLowerCase();
            if ("cape".equals(sub)) {
                for (Cape c : Cape.values()) if (c.getId().startsWith(partial)) out.add(c.getId());
            } else if ("particle".equals(sub) || "aura".equals(sub)) {
                for (ParticleEffect p : ParticleEffect.values()) if (p.getId().startsWith(partial)) out.add(p.getId());
            } else if ("unlock".equals(sub)) {
                for (String t : Arrays.asList("cape", "particle")) {
                    if (t.startsWith(partial)) out.add(t);
                }
            }
        }
        return out;
    }
}
