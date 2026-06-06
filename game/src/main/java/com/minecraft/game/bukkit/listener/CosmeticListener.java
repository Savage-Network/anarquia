package com.minecraft.game.bukkit.listener;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.cosmetic.ParticleEffect;
import com.minecraft.game.bukkit.cosmetic.CapeParticleRegistry;
import com.minecraft.game.bukkit.cosmetic.CosmeticParticleRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CosmeticListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!player.isOnline()) return;
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) return;

            CapeParticleRegistry.refresh(player.getUniqueId(), account.getActiveCape());
            CosmeticParticleRegistry.refresh(player.getUniqueId(), account.getActiveParticle());
        }, 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        CapeParticleRegistry.clear(event.getPlayer().getUniqueId());
        CosmeticParticleRegistry.clear(event.getPlayer().getUniqueId());
    }
}
