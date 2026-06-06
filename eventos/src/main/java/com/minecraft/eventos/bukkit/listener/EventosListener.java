package com.minecraft.eventos.bukkit.listener;

import com.minecraft.eventos.Constant;
import com.minecraft.eventos.Core;
import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.bukkit.display.AboveNameDisplay;
import com.minecraft.eventos.bukkit.manager.PermissionManager;
import com.minecraft.eventos.bukkit.manager.TagManager;
import com.minecraft.eventos.engine.EventEngine;
import com.minecraft.eventos.engine.EventModeRules;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.game.state.GameState;
import com.minecraft.eventos.game.variable.Variable;
import com.minecraft.eventos.message.EventMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
            event.joinMessage(EventMessages.component("event.player.join",
                    java.util.Map.of("player", player.getName())));
        } else {
            event.joinMessage(null);
        }

        EventEngine engine = EventosPlugin.getInstance().getEventEngine();
        EventSession session = engine.getSession();

        if (session.getState() == GameState.PLAYING) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(EventMessages.component("event.player.late-join"));
        } else {
            player.setGameMode(GameMode.ADVENTURE);
            if (session.isAutomated() && session.getState() == GameState.WAITING) {
                engine.joinQueue(player);
            }
        }

        if (session.getSpawn() != null) {
            player.teleport(session.getSpawn());
        } else if (session.getArena() != null && session.getArena().primarySpawn() != null) {
            player.teleport(session.getArena().primarySpawn());
        }

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
        event.quitMessage(EventMessages.component("event.player.quit",
                java.util.Map.of("player", event.getPlayer().getName())));
        PermissionManager.unloadPermissions(event.getPlayer());
        TagManager.removeTag(event.getPlayer());
        AboveNameDisplay.remove(event.getPlayer());
        EventEngine engine = EventosPlugin.getInstance().getEventEngine();
        engine.leaveQueue(event.getPlayer());
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(),
                () -> Core.getAccountController().unload(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        EventSession session = EventosPlugin.getInstance().getEventEngine().getSession();
        if (session.getState() != GameState.PLAYING || session.getActiveMode() == null) return;
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        int dieY = session.getArena() != null ? session.getArena().getDieY()
                : Variable.getInt(Variable.Key.DIE_Y);
        if (session.getState() == GameState.PLAYING && session.getActiveMode() != null) {
            session.getActiveMode().onPlayerMove(session, p, dieY);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        EventSession session = EventosPlugin.getInstance().getEventEngine().getSession();
        EventModeRules rules = rules(session, event.getPlayer());
        if (!rules.blockBreak() && !event.getPlayer().hasPermission("eventos.admin")) {
            event.setCancelled(true);
            return;
        }
        if (session.getState() == GameState.PLAYING && session.getActiveMode() != null) {
            session.getActiveMode().onBlockBreak(session, event);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        EventModeRules rules = rules(session(event.getPlayer()), event.getPlayer());
        if (!rules.blockPlace() && !event.getPlayer().hasPermission("eventos.admin")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        EventModeRules rules = rules(session(event.getPlayer()), event.getPlayer());
        if (!rules.dropItems()) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        EventModeRules rules = rules(session(event.getPlayer()), event.getPlayer());
        if (!rules.buckets()) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        EventModeRules rules = rules(session(event.getPlayer()), event.getPlayer());
        if (!rules.buckets()) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        EventSession session = session(player);
        EventModeRules rules = rules(session, player);
        if (!rules.damage()) {
            event.setCancelled(true);
            return;
        }
        if (event instanceof EntityDamageByEntityEvent byEntity
                && session.getState() == GameState.PLAYING
                && session.getActiveMode() != null) {
            session.getActiveMode().onDamage(session, byEntity);
            if (!rules.friendlyFire() && byEntity.getDamager() instanceof Player attacker) {
                var teamA = session.getContext().getTeams().get(player.getUniqueId());
                var teamB = session.getContext().getTeams().get(attacker.getUniqueId());
                if (teamA != null && teamA.equals(teamB)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!rules(session(player), player).food()) event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        EventSession session = session(event.getEntity());
        if (session.getState() != GameState.PLAYING || session.getActiveMode() == null) return;
        if (!session.activeRules().respawn()) return;
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            session.getContext().addKill(killer.getUniqueId());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        EventSession session = session(event.getPlayer());
        if (session.getState() != GameState.PLAYING) return;
        if (!session.activeRules().respawn()) return;
        if (session.getSpawn() != null) {
            event.setRespawnLocation(session.getSpawn());
        }
    }

    private EventSession session(Player player) {
        return EventosPlugin.getInstance().getEventEngine().getSession();
    }

    private EventModeRules rules(EventSession session, Player player) {
        if (player.hasPermission("eventos.admin")) {
            return new EventModeRules(true, true, true, true, true, true, true, true);
        }
        if (session.getState() == GameState.PLAYING && session.getActiveMode() != null) {
            return session.activeRules();
        }
        return new EventModeRules(
                com.minecraft.eventos.EventosSettings.RULE_DAMAGE,
                com.minecraft.eventos.EventosSettings.RULE_FOOD,
                com.minecraft.eventos.EventosSettings.RULE_DROP_ITEMS,
                com.minecraft.eventos.EventosSettings.RULE_BLOCK_PLACE,
                com.minecraft.eventos.EventosSettings.RULE_BLOCK_BREAK,
                com.minecraft.eventos.EventosSettings.RULE_BUCKETS,
                true,
                false);
    }
}
