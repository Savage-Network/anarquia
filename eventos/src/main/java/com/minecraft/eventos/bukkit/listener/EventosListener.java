package com.minecraft.eventos.bukkit.listener;

import com.minecraft.eventos.Constant;
import com.minecraft.eventos.Core;
import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.EventosSettings;
import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.bukkit.display.AboveNameDisplay;
import com.minecraft.eventos.bukkit.manager.PermissionManager;
import com.minecraft.eventos.bukkit.manager.TagManager;
import com.minecraft.eventos.game.Game;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.game.variable.Variable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener principal do servidor de eventos.
 *
 * <p>Aplica as flags do {@code config.yml > rules:} (espelha
 * {@code ServerOptions} do Savage), gerencia spectator e eliminacao
 * por queda no modo Sumo.
 */
public final class EventosListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        try {
            Account account = Core.getAccountController()
                    .load(event.getUniqueId(), event.getName())
                    .get();
            account.setIpAddress(event.getAddress().getHostAddress());
            account.updateLastLogin();
        } catch (Throwable t) {
            Core.getLogger().severe("Falha ao carregar conta de " + event.getName()
                    + ": " + t.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account != null && account.shouldBroadcastJoinMessage()) {
            event.joinMessage(Component.text("§e+ §a" + player.getName() + " §7entrou no evento."));
        } else {
            event.joinMessage(null);
        }

        Game game = EventosPlugin.getInstance().getGame();
        if (game.getState() == GameState.PLAYING) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(Component.text("§eVoce entrou como espectador (evento em andamento)."));
        } else {
            player.setGameMode(GameMode.ADVENTURE);
        }
        if (game.getSpawn() != null) player.teleport(game.getSpawn());

        PermissionManager.loadPermissions(player);

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            Account loaded = Core.getAccountController().of(player.getUniqueId());
            if (loaded == null) return;
            TagManager.updateTag(loaded);
            TagManager.updateAllForViewer(player);
            AboveNameDisplay.refresh(player);
            com.minecraft.eventos.bukkit.skin.SkinService.refreshFromMojangIfStale(loaded, player);
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(Component.text("§e- §c" + event.getPlayer().getName() + " §7saiu."));
        PermissionManager.unloadPermissions(event.getPlayer());
        TagManager.removeTag(event.getPlayer());
        AboveNameDisplay.remove(event.getPlayer());
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(event.getPlayer().getUniqueId()));
    }

    /* ----- modo Sumo: queda elimina ----- */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Game game = EventosPlugin.getInstance().getGame();
        if (!game.isSumo() || game.getState() != GameState.PLAYING) return;
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        int dieY = Variable.getInt(Variable.Key.DIE_Y);
        if (p.getLocation().getBlockY() < dieY) {
            p.setGameMode(GameMode.SPECTATOR);
            p.sendMessage(Component.text("§cVoce foi eliminado!"));
            game.broadcast("§7" + p.getName() + " §efoi eliminado.");
            if (game.alives().size() == 1) {
                Player winner = game.alives().get(0);
                game.end(winner.getUniqueId());
            }
        }
    }

    /* ----- rules: config.yml ----- */

    private boolean ruleBlocksBuild() {
        return !EventosSettings.RULE_BLOCK_PLACE;
    }

    private boolean ruleBlocksBreak() {
        return !EventosSettings.RULE_BLOCK_BREAK;
    }

    @EventHandler public void onPlace(BlockPlaceEvent e) {
        if (ruleBlocksBuild() && !e.getPlayer().hasPermission("eventos.admin")) e.setCancelled(true);
    }
    @EventHandler public void onBreak(BlockBreakEvent e) {
        if (ruleBlocksBreak() && !e.getPlayer().hasPermission("eventos.admin")) e.setCancelled(true);
    }
    @EventHandler public void onDrop(PlayerDropItemEvent e) {
        if (!EventosSettings.RULE_DROP_ITEMS) e.setCancelled(true);
    }
    @EventHandler public void onBucketFill(PlayerBucketFillEvent e) {
        if (!EventosSettings.RULE_BUCKETS) e.setCancelled(true);
    }
    @EventHandler public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!EventosSettings.RULE_BUCKETS) e.setCancelled(true);
    }
    @EventHandler public void onDamage(EntityDamageEvent e) {
        if (!EventosSettings.RULE_DAMAGE) e.setCancelled(true);
    }
    @EventHandler public void onFood(FoodLevelChangeEvent e) {
        if (!EventosSettings.RULE_FOOD) e.setCancelled(true);
    }
}
