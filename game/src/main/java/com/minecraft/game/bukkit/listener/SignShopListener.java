package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.shop.SignShopManager;
import com.minecraft.game.bukkit.shop.SignShopConfig;
import com.minecraft.game.bukkit.shop.SignShopParser;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SignShopListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign sign)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        String result = SignShopManager.processClick(player, sign, player.isSneaking());
        if (result != null) player.sendMessage(result);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = new String[4];
        var legacy = LegacyComponentSerializer.legacySection();
        for (int i = 0; i < 4; i++) {
            lines[i] = SignShopConfig.stripColor(
                    event.line(i) == null ? "" : legacy.serialize(event.line(i)));
        }

        if (SignShopParser.parseLines(lines, event.getBlock().getLocation(), player).isEmpty()) {
            return;
        }

        Sign sign = (Sign) event.getBlock().getState();
        player.sendMessage(SignShopManager.tryCreate(sign, player));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        SignShopManager.getShop(block).ifPresent(shop -> {
            if (!event.getPlayer().getUniqueId().equals(shop.getOwnerId())
                    && !event.getPlayer().hasPermission("game.shop.admin")
                    && !shop.isServerShop()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cEsta loja pertence a outro jogador.");
                return;
            }
            SignShopManager.unregister(block);
            event.getPlayer().sendMessage("§eLoja removida.");
        });
    }
}
