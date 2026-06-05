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
            description = "Abre o menu de capas ou equipa uma capa", usage = "/cosmetic [cape|off] [nome]")
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
            CosmeticManager.getCapeRenderer().remove(ctx.player());
            ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            ctx.reply("§aAsas desativadas!");
            return;
        }

        if (ctx.getArgs().length < 2) {
            ctx.reply("§cArgumento insuficiente. Uso: /cosmetic <cape|off> [nome]");
            return;
        }

        String name = ctx.getArgs()[1].toLowerCase();

        if ("cape".equals(sub) || "capa".equals(sub)) {
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
            ctx.reply("§aAsas " + cape.getDisplayName() + " §aequipadas!");
            ctx.player().playSound(ctx.player().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
        } else {
            ctx.reply("§cSubcomando inválido. Use: /cosmetic <cape|off>");
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
            for (String sub : Arrays.asList("cape", "off")) {
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
            }
        }

        return out;
    }
}
