package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Constant;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.manager.TeleportManager;
import com.minecraft.game.bukkit.manager.TeleportRequest;
import com.minecraft.game.bukkit.manager.TeleportRequestType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TpaCommand implements CommandInheritor {

    @Command(name = "tpa", playerOnly = true, description = "Envia uma solicitação de teleporte para outro jogador", usage = "/tpa <jogador>")
    public void tpa(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length == 0) {
            ctx.reply("§cUtilize: /tpa <jogador>");
            return;
        }

        Player sender = ctx.player();
        if (CombatTagManager.isTagged(sender)) {
            ctx.reply(CombatTagManager.blockedMessage(sender));
            return;
        }

        Player target = Bukkit.getPlayer(ctx.getArgs()[0]);

        if (target == null || !target.isOnline()) {
            ctx.reply("§cJogador offline ou inexistente.");
            return;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            ctx.reply("§cVocê não pode enviar solicitação para si mesmo.");
            return;
        }

        if (account.hasCooldown("tpa-cmd")) {
            long left = account.getCooldownRemaining("tpa-cmd");
            ctx.reply("§cAguarde " + String.format("%.1f", left / 1000.0) + "s para enviar outro pedido de teleporte.");
            return;
        }

        TeleportManager.sendRequest(sender.getUniqueId(), target.getUniqueId(), TeleportRequestType.TPA);
        account.setCooldown("tpa-cmd", 5000L);

        ctx.reply("§aVocê enviou uma solicitação de teleporte para §f" + target.getName() + "§a. Válida por 60 segundos.");

        Component interactive = Component.text("\n§6§lTELEPORTE " + Constant.ARROW_SYMBOL + " §f" + sender.getName() + " §equer se teleportar até você.\n")
                .append(Component.text("§eEscolha uma opção: "))
                .append(Component.text("§a§l[ACEITAR] ")
                        .clickEvent(ClickEvent.runCommand("/tpaccept " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("§aClique para aceitar o teleporte"))))
                .append(Component.text("§c§l[RECUSAR]\n")
                        .clickEvent(ClickEvent.runCommand("/tpdeny " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("§cClique para recusar o teleporte"))));
        target.sendMessage(interactive);
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @Command(name = "tpahere", playerOnly = true, description = "Envia uma solicitação para outro jogador se teleportar até você", usage = "/tpahere <jogador>")
    public void tpahere(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }

        if (ctx.getArgs().length == 0) {
            ctx.reply("§cUtilize: /tpahere <jogador>");
            return;
        }

        Player sender = ctx.player();
        if (CombatTagManager.isTagged(sender)) {
            ctx.reply(CombatTagManager.blockedMessage(sender));
            return;
        }

        Player target = Bukkit.getPlayer(ctx.getArgs()[0]);

        if (target == null || !target.isOnline()) {
            ctx.reply("§cJogador offline ou inexistente.");
            return;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            ctx.reply("§cVocê não pode enviar solicitação para si mesmo.");
            return;
        }

        if (account.hasCooldown("tpa-cmd")) {
            long left = account.getCooldownRemaining("tpa-cmd");
            ctx.reply("§cAguarde " + String.format("%.1f", left / 1000.0) + "s para enviar outro pedido de teleporte.");
            return;
        }

        TeleportManager.sendRequest(sender.getUniqueId(), target.getUniqueId(), TeleportRequestType.TPA_HERE);
        account.setCooldown("tpa-cmd", 5000L);

        ctx.reply("§aVocê enviou uma solicitação de teleporte reverso para §f" + target.getName() + "§a. Válida por 60 segundos.");

        Component interactive = Component.text("\n§6§lTELEPORTE " + Constant.ARROW_SYMBOL + " §f" + sender.getName() + " §equer que você se teleporte até ele.\n")
                .append(Component.text("§eEscolha uma opção: "))
                .append(Component.text("§a§l[ACEITAR] ")
                        .clickEvent(ClickEvent.runCommand("/tpaccept " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("§aClique para aceitar o teleporte"))))
                .append(Component.text("§c§l[RECUSAR]\n")
                        .clickEvent(ClickEvent.runCommand("/tpdeny " + sender.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("§cClique para recusar o teleporte"))));
        target.sendMessage(interactive);
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @Command(name = "tpaccept", aliases = {"tpaceitar"}, playerOnly = true, description = "Aceita uma solicitação de teleporte pendente", usage = "/tpaccept [jogador]")
    public void tpaccept(BukkitCommandContext ctx) {
        Player target = ctx.player();
        if (CombatTagManager.isTagged(target)) {
            ctx.reply(CombatTagManager.blockedMessage(target));
            return;
        }
        TeleportRequest request;

        if (ctx.getArgs().length == 0) {
            request = TeleportManager.getLatestRequest(target.getUniqueId());
            if (request == null) {
                ctx.reply("§cVocê não possui nenhuma solicitação de teleporte ativa.");
                return;
            }
        } else {
            Player sender = Bukkit.getPlayer(ctx.getArgs()[0]);
            if (sender == null) {
                ctx.reply("§cJogador offline ou inexistente.");
                return;
            }
            request = TeleportManager.getRequest(target.getUniqueId(), sender.getUniqueId());
            if (request == null) {
                ctx.reply("§cVocê não possui solicitações pendentes de §f" + sender.getName() + "§c.");
                return;
            }
        }

        Player sender = Bukkit.getPlayer(request.getSender());
        if (sender == null || !sender.isOnline()) {
            ctx.reply("§cO remetente do teleporte está offline.");
            TeleportManager.removeRequest(request);
            return;
        }
        if (CombatTagManager.isTagged(sender)) {
            ctx.reply("§cO jogador esta em combate.");
            return;
        }

        TeleportManager.removeRequest(request);

        if (request.getType() == TeleportRequestType.TPA) {
            sender.teleport(target.getLocation());
            sender.sendMessage("§aTeleportando para §f" + target.getName() + "§a...");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            sender.getWorld().spawnParticle(Particle.PORTAL, sender.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            
            target.sendMessage("§aVocê aceitou a solicitação de teleporte de §f" + sender.getName() + "§a.");
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else {
            target.teleport(sender.getLocation());
            target.sendMessage("§aTeleportando para §f" + sender.getName() + "§a...");
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);

            sender.sendMessage("§f" + target.getName() + " §aaceitou sua solicitação e foi teleportado até você.");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }

    @Command(name = "tpdeny", aliases = {"tprecusar"}, playerOnly = true, description = "Recusa uma solicitação de teleporte pendente", usage = "/tpdeny [jogador]")
    public void tpdeny(BukkitCommandContext ctx) {
        Player target = ctx.player();
        TeleportRequest request;

        if (ctx.getArgs().length == 0) {
            request = TeleportManager.getLatestRequest(target.getUniqueId());
            if (request == null) {
                ctx.reply("§cVocê não possui nenhuma solicitação de teleporte ativa.");
                return;
            }
        } else {
            Player sender = Bukkit.getPlayer(ctx.getArgs()[0]);
            if (sender == null) {
                ctx.reply("§cJogador offline ou inexistente.");
                return;
            }
            request = TeleportManager.getRequest(target.getUniqueId(), sender.getUniqueId());
            if (request == null) {
                ctx.reply("§cVocê não possui solicitações pendentes de §f" + sender.getName() + "§c.");
                return;
            }
        }

        TeleportManager.removeRequest(request);

        Player sender = Bukkit.getPlayer(request.getSender());
        if (sender != null && sender.isOnline()) {
            sender.sendMessage("§c" + target.getName() + " §crecusou sua solicitação de teleporte.");
            sender.playSound(sender.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }

        ctx.reply("§aVocê recusou a solicitação de teleporte.");
    }

    @Completer(name = "tpa")
    public List<String> tabTpa(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(partial) && !p.getName().equalsIgnoreCase(ctx.getSender().getName())) {
                out.add(p.getName());
            }
        }
        return out;
    }

    @Completer(name = "tpahere")
    public List<String> tabTpaHere(BukkitCommandContext ctx) {
        return tabTpa(ctx);
    }

    @Completer(name = "tpaccept")
    public List<String> tabTpAccept(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        if (ctx.isPlayer()) {
            Player p = ctx.player();
            // Lista apenas remetentes que enviaram solicitações para esse jogador
            for (Player sender : Bukkit.getOnlinePlayers()) {
                if (TeleportManager.getRequest(p.getUniqueId(), sender.getUniqueId()) != null) {
                    if (sender.getName().toLowerCase().startsWith(partial)) {
                        out.add(sender.getName());
                    }
                }
            }
        }
        return out;
    }

    @Completer(name = "tpdeny")
    public List<String> tabTpDeny(BukkitCommandContext ctx) {
        return tabTpAccept(ctx);
    }
}
