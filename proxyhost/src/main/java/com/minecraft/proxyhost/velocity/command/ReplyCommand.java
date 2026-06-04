package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /reply (/r) - responde a ultima conversa registrada pelo {@link TellCommand}.
 */
public final class ReplyCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final ConcurrentHashMap<UUID, UUID> LAST = new ConcurrentHashMap<>();

    public static void registerLastConversation(UUID self, UUID other) {
        LAST.put(self, other);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player sender)) {
            src.sendMessage(legacy("&c/r so pode ser usado por jogadores."));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(legacy("&cUso: /r <mensagem...>"));
            return;
        }

        UUID otherId = LAST.get(sender.getUniqueId());
        if (otherId == null) {
            sender.sendMessage(legacy("&cVoce nao tem uma conversa recente para responder."));
            return;
        }

        Player target = Core.getProxy().getPlayer(otherId).orElse(null);
        if (target == null) {
            sender.sendMessage(legacy("&cO jogador nao esta mais online."));
            LAST.remove(sender.getUniqueId());
            return;
        }

        Account senderAcc = Core.getAccountController().of(sender.getUniqueId());
        Account targetAcc = Core.getAccountController().of(otherId);
        if (senderAcc == null || targetAcc == null) {
            sender.sendMessage(legacy("&cSua conta ainda esta sendo carregada."));
            return;
        }

        if (!senderAcc.isVIP() && senderAcc.hasCooldown(Constant.TELL_COOLDOWN_KEY)) {
            long left = senderAcc.getCooldownRemaining(Constant.TELL_COOLDOWN_KEY) / 1000L;
            sender.sendMessage(legacy("&cAguarde &f" + Math.max(1, left) + "s &cantes de enviar outra mensagem."));
            return;
        }

        String message = String.join(" ", args);
        Component toTarget = legacy("&8(&fde &7" + senderAcc.getColoredName() + "&8) &7" + message);
        Component toSender = legacy("&8(&fpara &7" + targetAcc.getColoredName() + "&8) &7" + message);

        target.sendMessage(toTarget);
        sender.sendMessage(toSender);

        registerLastConversation(sender.getUniqueId(), otherId);
        registerLastConversation(otherId, sender.getUniqueId());

        if (!senderAcc.isVIP()) {
            senderAcc.setCooldown(Constant.TELL_COOLDOWN_KEY, Constant.TELL_COOLDOWN_MS);
        }
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
