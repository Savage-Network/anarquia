package com.minecraft.game.bukkit.listener;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.ClanSync;
import com.minecraft.game.bukkit.display.AboveNameDisplay;
import com.minecraft.game.bukkit.manager.PermissionManager;
import com.minecraft.game.bukkit.manager.TagManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Carrega a conta na pre-login (async) e a libera no quit.
 * Substitui {@code AccountListener} do Savage.
 */
public final class AccountListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        try {
            Account account = Core.getAccountController()
                    .load(event.getUniqueId(), event.getName())
                    .get(); // bloquear ate carregar

            account.setIpAddress(event.getAddress().getHostAddress());
            account.updateLastLogin();
        } catch (Throwable t) {
            Core.getLogger().severe("Erro ao carregar conta de " + event.getName()
                    + ": " + t.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) {
            Core.getLogger().warning("Conta nula no join de " + player.getName()
                    + " - tentando reload sincrono.");
            try {
                account = Core.getAccountController().load(player.getUniqueId(), player.getName()).get();
            } catch (Throwable t) {
                player.kick(Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
                return;
            }
        }

        // Mensagem de join basica
        Component msg = Component.text("§e+ §a" + player.getName() + " §7entrou no servidor.");
        event.joinMessage(msg);

        // Tab + nametag para todos os viewers (com TagPrefix individual)
        final Account loaded = account;
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (player.isOnline()) {
                PermissionManager.loadPermissions(player);
                ClanSync.refresh(loaded);
                TagManager.updateTag(loaded);
                // re-render dos demais sob o ponto de vista DESTE viewer
                TagManager.updateAllForViewer(player);
                AboveNameDisplay.refresh(player);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.quitMessage(Component.text("§e- §c" + player.getName() + " §7saiu do servidor."));

        PermissionManager.unloadPermissions(player);
        TagManager.removeTag(player);
        AboveNameDisplay.remove(player);

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(player.getUniqueId()));
    }
}
