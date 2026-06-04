package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.chat.PublicChatHandler;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class InfoListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        PublicChatHandler.handle(event);
    }
}
