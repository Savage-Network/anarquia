package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.api.punishment.Punishment;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import com.minecraft.proxyhost.backend.data.PunishmentData;
import com.minecraft.proxyhost.punishment.PunishmentService;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Bloqueia login se houver ban ativo (estilo Savage 1.8 {@code DataListener}).
 */
public final class PunishmentLoginListener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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
                        LEGACY.deserialize(PunishmentService.banKickMessage(punishment))));
            }
        } catch (Exception e) {
            Core.getLogger().warn("Falha ao verificar ban de {}: {}", player.getUsername(), e.getMessage());
            event.setResult(LoginEvent.ComponentResult.denied(
                    LEGACY.deserialize(Constant.ACCOUNT_LOAD_FAILED_MESSAGE)));
        }
    }
}
