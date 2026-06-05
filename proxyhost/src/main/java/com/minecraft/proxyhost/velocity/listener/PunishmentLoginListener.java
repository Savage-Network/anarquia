package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.api.punishment.Punishment;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import com.minecraft.proxyhost.backend.data.PunishmentData;
import com.minecraft.proxyhost.util.DisconnectMessages;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;

/**
 * Bloqueia login se houver ban ativo (estilo Savage 1.8 {@code DataListener}).
 */
public final class PunishmentLoginListener {

    @Subscribe(order = PostOrder.EARLY)
    public void onLoginBanCheck(LoginEvent event) {
        if (!event.getResult().isAllowed()) return;

        PunishmentData data = Core.getPunishmentData();
        if (data == null) return;

        var player = event.getPlayer();
        try {
            var ban = data.findActive(player.getUniqueId(), PunishmentCategory.BAN);
            if (ban.isPresent()) {
                Punishment punishment = ban.get();
                event.setResult(LoginEvent.ComponentResult.denied(
                        DisconnectMessages.ban(punishment)));
            }
        } catch (Exception e) {
            Core.getLogger().warn("Falha ao verificar ban de {}: {}", player.getUsername(), e.getMessage());
            event.setResult(LoginEvent.ComponentResult.denied(
                    DisconnectMessages.accountLoadFailed()));
        }
    }
}
