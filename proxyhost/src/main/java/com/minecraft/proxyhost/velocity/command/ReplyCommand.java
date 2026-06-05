package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.network.TellService;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplyCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final ConcurrentHashMap<UUID, UUID> LAST = new ConcurrentHashMap<>();

    public static void registerLastConversation(UUID self, UUID other) {
        if (self == null || other == null) return;
        LAST.put(self, other);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String[] args = invocation.arguments();

        if (!(src instanceof Player sender)) {
            src.sendMessage(legacy("&c/r só pode ser usado por jogadores."));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(legacy("&cUso: /r <mensagem...>"));
            return;
        }

        UUID otherId = LAST.get(sender.getUniqueId());
        if (otherId == null) {
            sender.sendMessage(legacy("&cVocê não tem uma conversa recente para responder."));
            return;
        }

        Player target = Core.getProxy().getPlayer(otherId).orElse(null);
        if (target == null) {
            sender.sendMessage(legacy("&cO jogador não está mais online."));
            LAST.remove(sender.getUniqueId());
            return;
        }

        String message = String.join(" ", args);
        TellService.SendContext ctx = TellService.prepare(sender, target.getUsername(), message);

        switch (ctx.result()) {
            case OK -> TellService.dispatch(ctx, sender);
            case SENDER_NOT_LOADED -> sender.sendMessage(legacy("&cSua conta ainda está sendo carregada."));
            case COOLDOWN -> sender.sendMessage(legacy("&cAguarde &f"
                    + TellService.cooldownSecondsRemaining(ctx.senderAcc())
                    + "s &cantes de enviar outra mensagem."));
            case TARGET_PM_DISABLED -> sender.sendMessage(legacy("&cEsse jogador não aceita mensagens privadas."));
            case BLOCKED -> sender.sendMessage(legacy("&cNão foi possível enviar (bloqueio ativo)."));
            case REDIS_OFFLINE -> sender.sendMessage(legacy("&cRedis offline — mensagens privadas indisponíveis."));
            default -> sender.sendMessage(legacy("&cNão foi possível enviar a resposta."));
        }
    }

    private net.kyori.adventure.text.Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
