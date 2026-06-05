package com.minecraft.auth.bukkit.listener;

import com.minecraft.auth.Constant;
import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.backend.data.AuthData;
import com.minecraft.auth.bukkit.AuthGuard;
import com.minecraft.auth.config.AuthConfig;
import com.minecraft.auth.message.AuthMessages;
import com.minecraft.auth.service.AuthService;
import com.minecraft.auth.bukkit.manager.PermissionManager;
import com.minecraft.auth.util.AuthRedirects;
import com.minecraft.auth.util.PlayerOriginDetector;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Servidor auth — bloqueia o jogador até login/registro e redireciona ao hub. */
public final class AuthListener implements Listener {

    private final Map<UUID, BukkitTask> reminderTasks = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        try {
            Account account = Core.getAccountController()
                    .load(event.getUniqueId(), event.getName())
                    .get();

            account.setIpAddress(event.getAddress().getHostAddress());
            account.updateLastLogin();

            AuthData authData = Core.getAuthData();
            if (authData != null) {
                authData.migrateFromContextIfNeeded(account);
            }

            AuthService.getInstance().initAttempts(account.getId());
        } catch (Throwable t) {
            Core.getLogger().severe("Falha ao carregar conta de " + event.getName() + ": " + t.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    AuthMessages.component("account.load-failed", Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            player.kick(AuthMessages.component("account.load-failed", Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
            return;
        }

        PermissionManager.loadPermissions(player);

        if (AuthConfig.get().isDevBypassAuth()) {
            handleDevBypass(player, account);
            return;
        }

        if (PlayerOriginDetector.isMojangOriginal(player)) {
            handleOriginalBypass(player, account);
            return;
        }

        boolean registered = AuthService.getInstance().isRegistered(account);
        showAuthTitle(player, registered);
        startReminder(player, registered);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        Player player = event.getPlayer();
        cancelReminder(player.getUniqueId());
        PermissionManager.unloadPermissions(player);
        AuthService.getInstance().clear(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(player.getUniqueId()));
    }

    private void handleDevBypass(Player player, Account account) {
        AuthService svc = AuthService.getInstance();
        int min = AuthConfig.get().getMinPasswordLength();
        if (!svc.isRegistered(account)) {
            svc.register(account, "dev", "dev");
        } else {
            svc.markSessionAuthenticated(account);
        }
        player.sendMessage(AuthMessages.component("auth.dev-bypass",
                "§a[DEV] Auth bypass — redirecionando..."));
        AuthRedirects.toHub(player);
    }

    /** Conta Mojang original — nao usa /login; vai direto ao lobby. */
    private void handleOriginalBypass(Player player, Account account) {
        account.setPremium(true);
        AuthService.getInstance().markSessionAuthenticated(account);
        account.markDirty();
        Core.getAccountData().saveAsync(account);
        player.sendMessage(AuthMessages.component("auth.original-bypass",
                "§aConta original detectada. Enviando ao lobby..."));
        AuthRedirects.toHub(player);
    }

    private void showAuthTitle(Player player, boolean registered) {
        Component title = AuthMessages.component("auth.title", "§6§lAUTENTICAÇÃO");
        Component subtitle = registered
                ? AuthMessages.component("auth.login-subtitle", "§7Use §a/login <senha>")
                : AuthMessages.component("auth.register-subtitle",
                "§7Use §a/register <senha> <confirmar>");

        player.showTitle(Title.title(title, subtitle,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500))));
        player.sendMessage(AuthMessages.component(registered ? "auth.login-chat" : "auth.register-chat",
                registered
                        ? "§6§lSavageMC §8» §7Digite §a/login <senha> §7para entrar."
                        : "§6§lSavageMC §8» §7Digite §a/register <senha> <confirmar> §7para criar conta."));
    }

    private void startReminder(Player player, boolean registered) {
        cancelReminder(player.getUniqueId());
        int interval = AuthConfig.get().getReminderIntervalTicks();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            if (!AuthGuard.isLocked(player)) {
                cancelReminder(player.getUniqueId());
                return;
            }
            player.sendActionBar(AuthMessages.component(registered ? "auth.login-actionbar" : "auth.register-actionbar",
                    registered
                            ? "§eAutentique-se: §a/login <senha>"
                            : "§eRegistre-se: §a/register <senha> <confirmar>"));
        }, interval, interval);
        reminderTasks.put(player.getUniqueId(), task);
    }

    private void cancelReminder(UUID id) {
        BukkitTask task = reminderTasks.remove(id);
        if (task != null) task.cancel();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        if (AuthGuard.isLocked(event.getPlayer())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!AuthGuard.isLocked(event.getPlayer())) return;
        if (!AuthGuard.isAllowedCommand(event.getMessage())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(AuthMessages.component("auth.blocked-command",
                    "§cAutentique-se primeiro com §e/login §cou §e/register§c."));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (AuthGuard.isLocked(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(AuthMessages.component("auth.blocked-chat",
                    "§cAutentique-se antes de conversar."));
        }
    }

    @EventHandler public void onPlace(BlockPlaceEvent e) { if (AuthGuard.isLocked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onBreak(BlockBreakEvent e) { if (AuthGuard.isLocked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onDrop(PlayerDropItemEvent e) { if (AuthGuard.isLocked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPickup(PlayerPickupItemEvent e) { if (AuthGuard.isLocked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && AuthGuard.isLocked(p)) e.setCancelled(true);
    }
    @EventHandler public void onInteract(PlayerInteractEvent e) { if (AuthGuard.isLocked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onInteractEntity(PlayerInteractEntityEvent e) { if (AuthGuard.isLocked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && AuthGuard.isLocked(p)) e.setCancelled(true);
    }
    @EventHandler public void onPvP(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p && AuthGuard.isLocked(p)) e.setCancelled(true);
        if (e.getDamager() instanceof Player p && AuthGuard.isLocked(p)) e.setCancelled(true);
    }
}
