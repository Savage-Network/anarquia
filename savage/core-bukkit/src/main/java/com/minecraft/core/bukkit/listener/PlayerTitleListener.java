package com.minecraft.core.bukkit.listener;

import com.minecraft.core.bukkit.BukkitCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerTitleListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (BukkitCore.getManager().getAboveName() != null) {
            BukkitCore.getManager().getAboveName().updateAboveName(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (BukkitCore.getManager().getAboveName() != null) {
            BukkitCore.getManager().getAboveName().removeAboveName(event.getPlayer());
        }
    }
}
