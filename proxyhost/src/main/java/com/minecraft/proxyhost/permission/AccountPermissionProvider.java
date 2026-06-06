package com.minecraft.proxyhost.permission;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;

/**
 * Provedor de permissoes do Velocity baseado no rank/conta SavageMC.
 * Consulta a {@link Account} em cache a cada verificacao (atualiza apos Redis sync).
 */
public final class AccountPermissionProvider implements PermissionProvider {

    private final PermissionProvider fallback;

    public AccountPermissionProvider(PermissionProvider fallback) {
        this.fallback = fallback;
    }

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        PermissionFunction fallbackFn = fallback.createFunction(subject);
        if (!(subject instanceof Player player)) {
            return fallbackFn;
        }

        return permission -> {
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account != null && account.grantsPermission(permission)) {
                return Tristate.TRUE;
            }
            return fallbackFn.getPermissionValue(permission);
        };
    }
}
