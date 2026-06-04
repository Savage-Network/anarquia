package com.minecraft.auth.bukkit.listener;

import com.minecraft.auth.Constant;
import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.service.AuthService;
import com.minecraft.auth.bukkit.manager.PermissionManager;
import com.minecraft.auth.util.AuthRedirects;
import net.kyori.adventure.text.Component;
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
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener principal do servidor auth. Bloqueia qualquer acao enquanto a
 * sessao nao estiver autenticada e dispara prompts visuais de
 * login/registro a cada poucos segundos.
 */
public final class AuthListener implements Listener {

    private static final java.util.Set<String> ALLOWED_COMMANDS = java.util.Set.of(
            "login", "logar", "entrar",
            "register", "registrar", "registar",
            "changepassword", "trocarsenha"
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        try {
            Account account = Core.getAccountController()
                    .load(event.getUniqueId(), event.getName())
                    .get();

            account.setIpAddress(event.getAddress().getHostAddress());
            account.updateLastLogin();

            int maxAttempts = com.minecraft.auth.AuthSettings.MAX_LOGIN_ATTEMPTS;
            AuthService.getInstance().initAttempts(account.getId(), maxAttempts);
        } catch (Throwable t) {
            Core.getLogger().severe("Falha ao carregar conta de " + event.getName()
                    + ": " + t.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            player.kick(Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
            return;
        }

        PermissionManager.loadPermissions(player);

        if (com.minecraft.auth.AuthSettings.DEV_BYPASS_AUTH) {
            AuthService svc = AuthService.getInstance();
            if (!svc.isRegistered(account)) {
                svc.register(account, "dev", "dev",
                        com.minecraft.auth.AuthSettings.MIN_PASSWORD_LENGTH);
            } else {
                svc.markSessionAuthenticated(account);
            }
            player.sendMessage(Component.text("§a[DEV] Auth bypass — redirecionando..."));
            AuthRedirects.toHub(player);
            return;
        }

        boolean registered = AuthService.getInstance().isRegistered(account);

        Component prompt = registered
                ? Component.text("§6§lAUTENTICACAO\n§7Use §a/login <senha> §7para entrar.")
                : Component.text("§6§lAUTENTICACAO\n§7Use §a/register <senha> <confirmar> §7para registrar.");

        player.sendMessage(prompt);

        // Lembrete periodico ate autenticar
        Bukkit.getScheduler().runTaskTimer(Core.getJavaPlugin(), task -> {
            if (!player.isOnline()) { task.cancel(); return; }
            if (AuthService.getInstance().isAuthenticated(player.getUniqueId())) { task.cancel(); return; }
            player.sendMessage(prompt);
        }, 200L, 200L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        PermissionManager.unloadPermissions(event.getPlayer());
        AuthService.getInstance().clear(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(event.getPlayer().getUniqueId()));
    }

    /* ---------- bloqueios de acao enquanto nao autenticado ---------- */

    private boolean locked(Player p) {
        return !AuthService.getInstance().isAuthenticated(p.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getZ() == event.getTo().getZ()) return;
        if (locked(event.getPlayer())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!locked(event.getPlayer())) return;
        String cmd = event.getMessage().substring(1).split(" ", 2)[0].toLowerCase();
        if (!ALLOWED_COMMANDS.contains(cmd)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cAutentique-se primeiro.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (locked(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cAutentique-se antes de conversar.");
        }
    }

    @EventHandler public void onPlace(BlockPlaceEvent e) { if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onBreak(BlockBreakEvent e) { if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onDrop(PlayerDropItemEvent e) { if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPickup(PlayerPickupItemEvent e) { if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onClick(InventoryClickEvent e) { if (e.getWhoClicked() instanceof Player p && locked(p)) e.setCancelled(true); }
    @EventHandler public void onInteract(PlayerInteractEvent e) { if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onInteractEntity(PlayerInteractEntityEvent e) { if (locked(e.getPlayer())) e.setCancelled(true); }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && locked(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p && locked(p)) e.setCancelled(true);
        if (e.getDamager() instanceof Player p && locked(p)) e.setCancelled(true);
    }
}
