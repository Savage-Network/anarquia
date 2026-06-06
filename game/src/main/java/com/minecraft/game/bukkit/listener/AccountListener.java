package com.minecraft.game.bukkit.listener;

import com.minecraft.game.Core;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.punishment.Punishment;
import com.minecraft.game.bukkit.display.AboveNameDisplay;
import com.minecraft.game.bukkit.display.JoinBroadcast;
import com.minecraft.game.bukkit.display.GameTabList;
import com.minecraft.game.bukkit.display.PlayerDisplayService;
import com.minecraft.game.bukkit.manager.PermissionManager;
import com.minecraft.game.bukkit.manager.TagManager;
import com.minecraft.game.bukkit.rank.RankGrantService;
import com.minecraft.game.bukkit.vanish.VanishService;
import com.minecraft.game.bukkit.welcome.WelcomeBookService;
import com.minecraft.game.punishment.PunishmentService;
import com.minecraft.game.progress.AchievementService;
import com.minecraft.game.util.AccountSync;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        try {
            Account account = Core.getAccountController()
                    .load(event.getUniqueId(), event.getName())
                    .get(); // bloquear ate carregar

            var ban = PunishmentService.activeBan(event.getUniqueId());
            if (ban.isPresent()) {
                Punishment punishment = ban.get();
                Core.getAccountController().unload(event.getUniqueId());
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        LEGACY.deserialize(PunishmentService.banKickMessage(punishment)));
                return;
            }

            account.setIpAddress(event.getAddress().getHostAddress());
            account.updateLastLogin();
        } catch (Throwable t) {
            Core.getLogger().severe("Erro ao carregar conta de " + event.getName()
                    + ": " + t.getMessage());
            Core.getAccountController().unload(event.getUniqueId());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Messages.component("account.load-failed"));
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
                player.kick(Messages.component("account.load-failed"));
                return;
            }
        }

        if (account != null && RankGrantService.refreshRankState(account)) {
            AccountSync.saveAndBroadcast(account);
        }

        if (account != null && !VanishService.willAutoVanishOnJoin(account)
                && !VanishService.isVanished(player)
                && account.shouldBroadcastJoinMessage()) {
            event.joinMessage(null);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.sendMessage(JoinBroadcast.create(
                        account, player, "join.game", LanguageService.of(viewer)));
            }
        } else {
            event.joinMessage(null);
        }

        player.sendMessage(Messages.component("join.hint", LanguageService.of(player)));

        // Tab + nametag para todos os viewers (com TagPrefix individual)
        final Account loaded = account;
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            WelcomeBookService.handleFirstJoin(loaded, player);
            AchievementService.onLogin(loaded);
            PlayerDisplayService.applyAll(loaded, player);
            com.minecraft.game.bukkit.skin.SkinService.refreshFromMojangIfStale(loaded, player);
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                Account otherAcc = Core.getAccountController().of(other.getUniqueId());
                if (otherAcc != null) TagManager.applyTagForViewer(player, other);
            }
            GameTabList.refreshAll();
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.quitMessage(null);

        PermissionManager.unloadPermissions(player);
        TagManager.removeTag(player);
        AboveNameDisplay.remove(player);
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), GameTabList::refreshAll, 1L);

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(player.getUniqueId()));
    }
}
