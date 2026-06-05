package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.vanish.VanishConfig;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.util.TabCompleteUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class VanishCommand implements CommandInheritor {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Command(name = "vanish", aliases = {"v"}, playerOnly = true,
            permission = "lobbyhost.vanish",
            description = "Modo invisível staff",
            usage = "/vanish [on|off|list|fakejoin|fakequit|reload]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta não carregada.");
            return;
        }
        if (!VanishService.canVanish(account)) {
            ctx.reply("§cSem permissão.");
            return;
        }

        Player player = ctx.player();
        String[] args = ctx.getArgs();

        if (args.length == 0) {
            VanishService.toggle(player);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "on", "true", "1", "ativar" -> VanishService.setVanished(player, true);
            case "off", "false", "0", "desativar" -> VanishService.setVanished(player, false);
            case "list", "lista" -> listVanished(ctx);
            case "fakejoin", "join" -> VanishService.sendFakeJoin(player);
            case "fakequit", "quit", "sair" -> VanishService.sendFakeQuit(player);
            case "reload" -> {
                if (!account.hasPermission("lobbyhost.admin")) {
                    ctx.reply("§cSem permissão.");
                    return;
                }
                VanishService.reload(Core.getJavaPlugin());
                ctx.reply("§avanish.yml recarregado.");
            }
            default -> ctx.reply("§e/vanish §7| §eon|off §7| §elist §7| §efakejoin §7| §efakequit");
        }
    }

    private void listVanished(BukkitCommandContext ctx) {
        var list = VanishService.vanishedOnline();
        if (list.isEmpty()) {
            ctx.player().sendMessage(ModernText.parse(VanishConfig.msgListEmpty()));
            return;
        }
        ctx.player().sendMessage(ModernText.parse(
                VanishConfig.msgListHeader().replace("{count}", String.valueOf(list.size()))));
        for (Player p : list) {
            Account targetAcc = Core.getAccountController().of(p.getUniqueId());
            String name = targetAcc != null ? targetAcc.getColoredName() : p.getName();
            ctx.player().sendMessage(LEGACY.deserialize("§8• " + name));
        }
    }

    @Completer(name = "vanish", subCommands = {"v"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0],
                    "on", "off", "list", "lista", "fakejoin", "fakequit", "reload");
        }
        return List.of();
    }
}
