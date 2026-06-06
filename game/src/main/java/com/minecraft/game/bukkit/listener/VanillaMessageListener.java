package com.minecraft.game.bukkit.listener;

import com.minecraft.game.message.DeathMessageService;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Map;

/** Substitui mensagens vanilla do Minecraft por versoes traduzidas por idioma. */
public final class VanillaMessageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        event.deathMessage(null);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.sendMessage(DeathMessageService.build(event, LanguageService.of(viewer)));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        AdvancementDisplay display = event.getAdvancement().getDisplay();
        if (display == null || !display.doesAnnounceToChat()) return;

        event.message(null);
        String playerName = event.getPlayer().getName();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            var lang = LanguageService.of(viewer);
            String title = DeathMessageService.advancementTitle(display.title(), lang);
            viewer.sendMessage(Messages.component("minecraft.advancement", lang, Map.of(
                    "player", playerName,
                    "title", title)));
        }
    }
}
