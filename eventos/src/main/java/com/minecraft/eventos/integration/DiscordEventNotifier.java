package com.minecraft.eventos.integration;

import com.minecraft.eventos.config.DiscordConfig;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.util.DiscordWebhookUtil;
import org.bukkit.Bukkit;

/** Notificacoes Discord de eventos. */
public final class DiscordEventNotifier {

    private DiscordEventNotifier() {}

    public static void started(EventSession session) {
        if (!DiscordConfig.feature("event-notifications")) return;
        String mode = session.getActiveMode() != null
                ? session.getActiveMode().displayName() : session.getModeId();
        String host = session.getHostName() != null ? session.getHostName() : "Staff";
        DiscordWebhookUtil.send("staff-alerts",
                "Modo: " + mode + "\nHost: " + host + "\nJogadores: " + Bukkit.getOnlinePlayers().size(),
                "Evento iniciado");
    }

    public static void ended(EventSession session) {
        if (!DiscordConfig.feature("event-notifications")) return;
        String mode = session.getActiveMode() != null
                ? session.getActiveMode().displayName() : session.getModeId();
        String winner = "?";
        if (session.getWinnerId() != null) {
            var p = Bukkit.getPlayer(session.getWinnerId());
            winner = p != null ? p.getName() : session.getWinnerId().toString();
        } else if (session.getWinningTeam() != null) {
            winner = "Time " + session.getWinningTeam().name();
        }
        DiscordWebhookUtil.send("staff-alerts",
                "Modo: " + mode + "\nVencedor: " + winner + "\nEstado: " + session.getState(),
                "Evento encerrado");
    }

    public static void scheduled(String taskId, String mode, int seconds) {
        if (!DiscordConfig.feature("event-notifications")) return;
        DiscordWebhookUtil.send("staff-alerts",
                "Tarefa: " + taskId + "\nModo: " + mode + "\nInicio em: " + seconds + "s",
                "Evento automatico agendado");
    }
}
