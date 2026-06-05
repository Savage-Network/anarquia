package com.minecraft.game.bukkit.listener;

import com.minecraft.game.Core;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Action bar com Ping, CPS esquerdo e CPS direito.
 * CPS é contado por janela de 1 segundo (20 ticks).
 */
public final class ActionBarListener implements Listener {

    // cliques na janela atual (1s)
    private static final Map<UUID, AtomicInteger> leftClicks  = new ConcurrentHashMap<>();
    private static final Map<UUID, AtomicInteger> rightClicks = new ConcurrentHashMap<>();

    // último CPS confirmado (ao fechar janela)
    private static final Map<UUID, Integer> lastLeftCps  = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lastRightCps = new ConcurrentHashMap<>();

    private static BukkitTask cpsWindowTask;
    private static BukkitTask actionBarTask;

    private ActionBarListener() {}

    /** Inicia as tasks; chamado no onEnable. */
    public static void start(JavaPlugin plugin) {
        // A cada 20 ticks (1s): fecha janela de CPS e zera contadores
        cpsWindowTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID id = player.getUniqueId();
                lastLeftCps.put(id,  leftClicks.getOrDefault(id,  new AtomicInteger(0)).getAndSet(0));
                lastRightCps.put(id, rightClicks.getOrDefault(id, new AtomicInteger(0)).getAndSet(0));
            }
        }, 20L, 20L);

        // A cada 4 ticks (~200ms): envia action bar atualizada
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Só exibe durante combate — o CombatTagManager cuida do próprio actionbar
                // ActionBar de ping/CPS desativado
            }
        }, 4L, 4L);
    }

    public static void stop() {
        if (cpsWindowTask != null) cpsWindowTask.cancel();
        if (actionBarTask  != null) actionBarTask.cancel();
    }

    // ---- Listeners ----

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;
        UUID id = event.getPlayer().getUniqueId();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            leftClicks.computeIfAbsent(id, k -> new AtomicInteger(0)).incrementAndGet();
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            rightClicks.computeIfAbsent(id, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        leftClicks.remove(id);
        rightClicks.remove(id);
        lastLeftCps.remove(id);
        lastRightCps.remove(id);
    }
}
