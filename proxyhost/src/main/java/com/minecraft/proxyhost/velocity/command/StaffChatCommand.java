package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * /staffchat (/sc) - chat exclusivo de staff visivel em todos os servidores.
 *
 * <p>Permissao: rank Staff (via {@link Account#isStaffer()}) ou node {@code proxyhost.staffchat}.
 */
public final class StaffChatCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player sender)) {
            src.sendMessage(legacy("&c/sc so pode ser usado por jogadores."));
            return;
        }
        Account a = Core.getAccountController().of(sender.getUniqueId());
        if (a == null || (!a.isStaffer() && !sender.hasPermission("proxyhost.staffchat"))) {
            sender.sendMessage(legacy("&cVoce nao tem permissao para usar o staff chat."));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(legacy("&cUso: /sc <mensagem...>"));
            return;
        }
        String prefix = com.minecraft.proxyhost.ProxySettings.STAFFCHAT_PREFIX;
        String msg = String.join(" ", args);
        Component formatted = legacy(prefix + "&f" + a.getColoredName() + "&7 (&f"
                + a.getCurrentServer() + "&7): &f" + msg);

        for (Player p : Core.getProxy().getAllPlayers()) {
            Account other = Core.getAccountController().of(p.getUniqueId());
            if (other != null && (other.isStaffer() || p.hasPermission("proxyhost.staffchat"))) {
                p.sendMessage(formatted);
            }
        }
        Core.getProxy().getConsoleCommandSource().sendMessage(formatted);
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
