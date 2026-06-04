package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /tell {@literal <player>} {@literal <mensagem...>}
 *
 * <p>Mensagem privada cross-server (formato Savage). Aplica cooldown via
 * {@link Account#setCooldown(String, long)} para nao-VIP.
 */
public final class TellCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player sender)) {
            src.sendMessage(legacy("&c/tell so pode ser usado por jogadores."));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(legacy("&cUso: /tell <jogador> <mensagem...>"));
            return;
        }

        Account senderAcc = Core.getAccountController().of(sender.getUniqueId());
        if (senderAcc == null) {
            sender.sendMessage(legacy("&cSua conta nao foi carregada. Tente reconectar."));
            return;
        }

        if (!senderAcc.isVIP() && senderAcc.hasCooldown(Constant.TELL_COOLDOWN_KEY)) {
            long left = senderAcc.getCooldownRemaining(Constant.TELL_COOLDOWN_KEY) / 1000L;
            sender.sendMessage(legacy("&cAguarde &f" + Math.max(1, left) + "s &cantes de enviar outra mensagem."));
            return;
        }

        String targetName = args[0];
        Player target = Core.getProxy().getPlayer(targetName).orElse(null);
        if (target == null) {
            sender.sendMessage(legacy("&cJogador &f" + targetName + " &cnao esta online."));
            return;
        }
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(legacy("&cVoce nao pode enviar mensagem para si mesmo."));
            return;
        }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) {
            sender.sendMessage(legacy("&cA conta do destinatario ainda esta sendo carregada."));
            return;
        }

        if (!targetAcc.getToggle().isAllowPrivateMessages()) {
            sender.sendMessage(legacy("&cEsse jogador nao aceita mensagens privadas."));
            return;
        }

        if (isBlockedEither(senderAcc, targetAcc)) {
            sender.sendMessage(legacy("&cNao foi possivel enviar (bloqueio ativo)."));
            return;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                .replace('|', ' ');

        Component toTarget = legacy("&8(&fde &7" + senderAcc.getColoredName() + "&8) &7" + message);
        Component toSender = legacy("&8(&fpara &7" + targetAcc.getColoredName() + "&8) &7" + message);

        target.sendMessage(toTarget);
        sender.sendMessage(toSender);

        senderAcc.getContext().setLastMessage(target.getUniqueId());
        targetAcc.getContext().setLastMessage(sender.getUniqueId());
        senderAcc.markDirty();
        targetAcc.markDirty();

        ReplyCommand.registerLastConversation(sender.getUniqueId(), target.getUniqueId());
        ReplyCommand.registerLastConversation(target.getUniqueId(), sender.getUniqueId());

        if (!senderAcc.isVIP()) {
            senderAcc.setCooldown(Constant.TELL_COOLDOWN_KEY, Constant.TELL_COOLDOWN_MS);
        }
    }

    private boolean isBlockedEither(Account a, Account b) {
        return hasBlocked(a, b.getId()) || hasBlocked(b, a.getId());
    }

    private boolean hasBlocked(Account account, java.util.UUID other) {
        if (account == null || other == null) return false;
        return account.getContext().getBlocks().stream()
                .anyMatch(bl -> bl.getId() != null && bl.getId().equals(other));
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            List<String> names = new ArrayList<>();
            String prefix = args.length == 1 ? args[0].toLowerCase() : "";
            for (Player p : Core.getProxy().getAllPlayers()) {
                if (p.getUsername().toLowerCase().startsWith(prefix)) names.add(p.getUsername());
            }
            return names;
        }
        return Collections.emptyList();
    }
}
