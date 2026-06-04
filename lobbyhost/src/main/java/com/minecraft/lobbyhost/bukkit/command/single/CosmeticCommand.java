package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.CosmeticTitle;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Gadget;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.ParticleEffect;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.manager.CosmeticManager;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.menu.CosmeticMenus;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CosmeticCommand implements CommandInheritor {

    @Command(name = "cosmetic", aliases = {"cosmeticos", "cosmetico", "cos"}, playerOnly = true,
            description = "Abre o menu de cosméticos ou equipa um individual", usage = "/cosmetic [subcomando] [nome]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }

        if (ctx.getArgs().length == 0) {
            CosmeticMenus.openMainMenu(ctx.player());
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase();
        if ("off".equalsIgnoreCase(sub) || "desativar".equalsIgnoreCase(sub)) {
            account.setActiveCape(null);
            account.setActiveParticle(null);
            account.setActiveCosmeticTitle(null);
            account.setActiveGadget(null);
            CosmeticManager.getCapeRenderer().remove(ctx.player());
            com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout.applyDefault(ctx.player());
            TagManager.updateTag(account);
            ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            ctx.reply("§aTodos os cosméticos foram desativados!");
            return;
        }

        if (ctx.getArgs().length < 2) {
            ctx.reply("§cArgumento insuficiente. Uso: /cosmetic <cape|particle|title|gadget> <nome>");
            return;
        }

        String name = ctx.getArgs()[1].toLowerCase();

        switch (sub) {
            case "cape":
            case "capa":
                Cape cape = Cape.fromId(name);
                if (cape == null) {
                    ctx.reply("§cCapa desconhecida.");
                    return;
                }
                if (!account.hasCape(cape)) {
                    ctx.reply("§cVocê não possui essa capa!");
                    return;
                }
                account.setActiveCape(cape);
                CosmeticManager.getCapeRenderer().spawn(ctx.player(), cape);
                ctx.reply("§aCapa " + cape.getDisplayName() + " §aequipada!");
                ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                break;

            case "particle":
            case "particula":
                ParticleEffect effect = ParticleEffect.fromId(name);
                if (effect == null) {
                    ctx.reply("§cPartícula desconhecida.");
                    return;
                }
                if (!account.hasParticle(effect)) {
                    ctx.reply("§cVocê não possui essa partícula!");
                    return;
                }
                account.setActiveParticle(effect);
                ctx.reply("§aPartícula " + effect.getDisplayName() + " §aequipada!");
                ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                break;

            case "title":
            case "titulo":
                CosmeticTitle title = CosmeticTitle.fromId(name);
                if (title == null) {
                    ctx.reply("§cTítulo desconhecido.");
                    return;
                }
                if (!account.hasCosmeticTitle(title)) {
                    ctx.reply("§cVocê não possui esse título!");
                    return;
                }
                account.setActiveCosmeticTitle(title);
                TagManager.updateTag(account);
                ctx.reply("§aTítulo " + title.getDisplayName() + " §aequipado!");
                ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                break;

            case "gadget":
                Gadget gadget = Gadget.fromId(name);
                if (gadget == null) {
                    ctx.reply("§cGadget desconhecido.");
                    return;
                }
                if (!account.hasGadget(gadget)) {
                    ctx.reply("§cVocê não possui esse gadget!");
                    return;
                }
                account.setActiveGadget(gadget);
                com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout.applyDefault(ctx.player());
                ctx.reply("§aGadget " + gadget.getDisplayName() + " §aequipado!");
                ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                break;

            default:
                ctx.reply("§cSubcomando inválido. Use: /cosmetic <cape|particle|title|gadget|off>");
                break;
        }
    }

    @Completer(name = "cosmetic")
    public List<String> tab(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) return new ArrayList<>();

        List<String> out = new ArrayList<>();
        String[] args = ctx.getArgs();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String sub : Arrays.asList("cape", "particle", "title", "gadget", "off")) {
                if (sub.startsWith(partial)) out.add(sub);
            }
            return out;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            if ("cape".equals(sub) || "capa".equals(sub)) {
                for (Cape cape : Cape.values()) {
                    if (account.hasCape(cape) && cape.getId().startsWith(partial)) {
                        out.add(cape.getId());
                    }
                }
            } else if ("particle".equals(sub) || "particula".equals(sub)) {
                for (ParticleEffect p : ParticleEffect.values()) {
                    if (account.hasParticle(p) && p.getId().startsWith(partial)) {
                        out.add(p.getId());
                    }
                }
            } else if ("title".equals(sub) || "titulo".equals(sub)) {
                for (CosmeticTitle t : CosmeticTitle.values()) {
                    if (account.hasCosmeticTitle(t) && t.getId().startsWith(partial)) {
                        out.add(t.getId());
                    }
                }
            } else if ("gadget".equals(sub)) {
                for (Gadget g : Gadget.values()) {
                    if (account.hasGadget(g) && g.getId().startsWith(partial)) {
                        out.add(g.getId());
                    }
                }
            }
        }

        return out;
    }
}
