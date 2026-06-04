package com.minecraft.lobbyhost.bukkit.lobby;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.skin.SkinMetadata;
import com.minecraft.lobbyhost.bukkit.manager.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Setup pos-join estilo {@code DataListener} do Savage (skin + permissoes Bukkit).
 */
public final class LobbyPlayerSetup {

    private LobbyPlayerSetup() {}

    public static void apply(Player player, Account account) {
        if (account == null) return;
        PermissionManager.loadPermissions(player);
        applySkin(player, account);
    }

    public static void applySkin(Player player, Account account) {
        SkinMetadata skin = account.getSkin();
        if (skin == null || !skin.isValid()) return;
        try {
            PlayerProfile profile = player.getPlayerProfile();
            profile.setProperty(new ProfileProperty("textures", skin.getValue(),
                    skin.getSignature() != null ? skin.getSignature() : ""));
            player.setPlayerProfile(profile);
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[lobbyhost] Falha ao aplicar skin: " + t.getMessage());
        }
    }
}
