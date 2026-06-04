package com.minecraft.lobby.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.account.AccountClanChangeEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    private final Architect architect;

    public ServerListener() {
        this.architect = Lobby.getLoader().getArchitect();
    }

    @EventHandler
    public void onAsyncCounterUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            ServerType realServer = architect.getServer().getRealServerOfLobby();

            if (realServer != null) {
                for (ArcadeCategory arcade : ArcadeCategory.list(category -> category.getServer().equals(realServer))) {
                    HologramServer hologram = BukkitCore.getManager().getHologram().getServer(arcade.getId());

                    if (hologram != null) {
                        int onlinePlayers = Core.getArcadeData().getOnlinePlayers(arcade);

                        hologram.setText(hologram.getRows().size() - 1, "§e" + Util.formatNumber(onlinePlayers) + " jogando.");
                    }
                }
            }

            // Atualizar players na scoreboard
            for (UserModel user : User.getList()) {
                if (user != null) {
                    Sidebar sidebar = user.getSidebar();

                    if (sidebar != null)
                        sidebar.updateRow("players", "Jogadores: §a" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));
                }
            }
        }
    }

    @EventHandler
    public void onVoid(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            event.setCancelled(true);

            Location spawn = architect.getLocation("spawn");

            if (spawn != null)
                player.teleport(spawn);
        }
    }

    @EventHandler
    public void onAccountClan(AccountClanChangeEvent event) {
        Account account = event.getAccount();

        User user = (User) User.of(account.getId());

        if (user != null)
            user.getSidebar().updateRow("clan", "Clan: " + (account.hasClan() ? account.getClanTag().trim() : "§cNenhum"));
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        architect.chat(player, event.getMessage());
    }

    @EventHandler
    public void onSlimeJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Material down = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType();

        if (down.equals(Material.SLIME_BLOCK)) {
            player.setVelocity(architect.getSlimeJump(player.getLocation().getDirection()));

            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
        }
    }

    @EventHandler
    public void onVisibilityItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return;

        ItemStack hand = event.getItem();

        if (event.hasBlock() && event.getClickedBlock().getType().name().contains("BUTTON"))
            event.setCancelled(true);

        if (event.hasItem() && hand != null) {
            if (hand.getType().equals(Material.INK_SACK)) {
                if (account.hasCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)) {
                    account.send("§cAguarde " + account.getFormattedCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY)
                            + " para alterar a visibilidade dos jogadores novamente.");
                    return;
                }

                User lobbyUser = (User) User.of(player.getUniqueId());
                if (lobbyUser == null) return;

                // Alternar para o próximo modo
                com.minecraft.lobby.user.visibility.PlayerVisibilityMode currentMode = lobbyUser.getVisibilityMode();
                com.minecraft.lobby.user.visibility.PlayerVisibilityMode nextMode = currentMode.next();
                lobbyUser.setVisibilityMode(nextMode);

                // Atualizar item na mão
                player.setItemInHand(Item.of(nextMode.getMaterial(), nextMode.getData(), nextMode.getDisplayName()));
                player.updateInventory();

                // Aplicar visibilidade
                Bukkit.getOnlinePlayers().forEach(online -> {
                    if (online.equals(player)) return;
                    
                    switch (nextMode) {
                        case ALL:
                            player.showPlayer(online);
                            break;
                        case FRIENDS_ONLY:
                            if (account.isFriend(online.getUniqueId())) {
                                player.showPlayer(online);
                            } else {
                                player.hidePlayer(online);
                            }
                            break;
                        case NONE:
                            player.hidePlayer(online);
                            break;
                    }
                });

                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 2.0f);
                account.setCooldown(Constant.PLAYERS_VISIBILITY_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(5));
            }
        }
    }
}
