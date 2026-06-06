package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.permission.AccountPermissionProvider;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.proxy.Player;

import java.util.concurrent.TimeUnit;

/**
 * Instala permissoes da conta SavageMC no Velocity ({@code Player#hasPermission}).
 */
public final class PermissionsSetupListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPermissionsSetup(PermissionsSetupEvent event) {
        PermissionSubject subject = event.getSubject();
        event.setProvider(new AccountPermissionProvider(event.getProvider()));

        if (!(subject instanceof Player player)) return;

        try {
            Core.getAccountController()
                    .load(player.getUniqueId(), player.getUsername())
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Core.getLogger().warn("[Perm] Falha ao carregar conta de {}: {}",
                    player.getUsername(), e.getMessage());
        }
    }
}
