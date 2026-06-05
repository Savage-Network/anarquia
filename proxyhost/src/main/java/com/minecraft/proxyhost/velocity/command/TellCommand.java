package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.network.TellService;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TellCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player sender)) {
            src.sendMessage(legacy("&c/tell só pode ser usado por jogadores."));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(legacy("&cUso: /tell <jogador> <mensagem...>"));
            return;
        }

        String targetName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        TellService.SendContext ctx = TellService.prepare(sender, targetName, message);

        switch (ctx.result()) {
            case OK -> {
                TellService.dispatch(ctx, sender);
            }
            case SENDER_NOT_LOADED -> sender.sendMessage(legacy("&cSua conta não foi carregada. Tente reconectar."));
            case COOLDOWN -> sender.sendMessage(legacy("&cAguarde &f"
                    + TellService.cooldownSecondsRemaining(ctx.senderAcc())
                    + "s &cantes de enviar outra mensagem."));
            case TARGET_OFFLINE -> sender.sendMessage(legacy("&cJogador &f" + targetName + " &cnão está online."));
            case SELF -> sender.sendMessage(legacy("&cVocê não pode enviar mensagem para si mesmo."));
            case TARGET_NOT_LOADED -> sender.sendMessage(legacy("&cA conta do destinatário ainda está sendo carregada."));
            case TARGET_PM_DISABLED -> sender.sendMessage(legacy("&cEsse jogador não aceita mensagens privadas."));
            case BLOCKED -> sender.sendMessage(legacy("&cNão foi possível enviar (bloqueio ativo)."));
            case REDIS_OFFLINE -> sender.sendMessage(legacy("&cRedis offline — mensagens privadas indisponíveis."));
        }
    }

    private net.kyori.adventure.text.Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            List<String> names = new ArrayList<>();
            String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            for (Player p : com.minecraft.proxyhost.Core.getProxy().getAllPlayers()) {
                if (p.getUsername().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    names.add(p.getUsername());
                }
            }
            return names;
        }
        return Collections.emptyList();
    }
}
