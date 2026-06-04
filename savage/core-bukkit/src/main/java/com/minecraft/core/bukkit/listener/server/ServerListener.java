package com.minecraft.core.bukkit.listener.server;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.menu.server.arcade.recraft.RecraftMenu;
import com.minecraft.core.bukkit.menu.server.arcade.recraft.SoupMenu;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.server.Server;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.TimeUtil;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    private final List<Material> DANGEROUS_ITEMS = Collections.singletonList(Material.TNT);

    private final List<EntityType> BLOCKED_ENTITIES = Arrays.asList(EntityType.SLIME, EntityType.CREEPER, EntityType.PIG);

    private final List<Material> ALLOWED_ITEM_DROPS = Arrays.asList(
            Material.MUSHROOM_SOUP, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.BOWL);

    @EventHandler(ignoreCancelled = true)
    public void onProhibitedInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.hasBlock()) {
            Player player = event.getPlayer();

            Block clicked = event.getClickedBlock();

            if (clicked.getType().name().contains("SIGN")) {
                Sign sign = (Sign) clicked.getState();

                String[] lines = sign.getLines();

                for (String line : lines) {
                    if (line.contains("Sopa")) {
                        new SoupMenu(player).handle();
                        break;
                    }

                    if (line.contains("Recraft")) {
                        new RecraftMenu(player).handle();
                        break;
                    }
                }
            }

            if (!ServerOptions.BLOCK_INTERACTION_ENABLED && ServerOptions.NON_INTERACTIVE_BLOCKS.contains(clicked.getType())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();

        if (item == null) return;

        if (ServerOptions.DROP_ITEM_ENABLED) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(!ALLOWED_ITEM_DROPS.contains(item.getItemStack().getType()));
    }

    @EventHandler
    public void onBedSpawn(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();

        if (stack != null && (stack.getType().equals(Material.BED) || stack.getType().equals(Material.BED_BLOCK)))
            event.getEntity().remove();
    }

    @EventHandler
    public void onClearDrop(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            /* Verificando drops do servidor */
            if (ServerOptions.AUTO_CLEAR_DROPS) {
                for (World world : Bukkit.getServer().getWorlds()) {
                    for (Entity e : world.getEntitiesByClass(Item.class)) {
                        if (e.getTicksLived() >= 80) {
                            Location location = e.getLocation().clone();

                            world.playEffect(location, Effect.FLAME, 10);
                            e.remove();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Server server = Core.getServerData().local();

        if (server != null && server.isArcade() && event.getAction().name().contains("RIGHT")) {
            ItemStack item = player.getItemInHand();

            if (item != null && item.getType().equals(Material.MUSHROOM_SOUP) &&
                    (player.getHealth() != player.getMaxHealth() || player.getFoodLevel() != 20)) {

                double lifeToRescue = Math.min(player.getHealth() + 7, player.getMaxHealth());
                double previousHealth = player.getHealth();

                player.setHealth(lifeToRescue);
                player.setFoodLevel(20);

                // Calcula a quantidade exata de vida recuperada
                double healthRecovered = (lifeToRescue - previousHealth) / 2;

                ProtocolHandler.sendBar(player, "§c+" + Util.formatNumberWithComma(healthRecovered) + " §4❤");

                player.getItemInHand().setType(Material.BOWL);
                player.updateInventory();
            }
        }
    }

    protected boolean cancelBuild(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) return true;

        return !(account.hasRank(RankType.BUILDER) || account.hasRank(RankType.ADMIN))
                || !player.getGameMode().equals(GameMode.CREATIVE) || !account.getToggle().isAllowBuild();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        Account account = Core.getAccountController().of(event.getPlayer().getUniqueId());

//        if (account.inServer(ServerType.DUELS) && account.getArcade().isValid()
//                && account.getArcade().getArcade().hasOption(ArcadeOption.DANGEROUS_BLOCKS) && DANGEROUS_ITEMS.contains(block.getType())) {
//            event.setCancelled(false);
//            return;
//        }

        if (!ServerOptions.TNT_PLACE && block.getType().equals(Material.TNT)) {
            event.setCancelled(true);
            return;
        }

        if (ServerOptions.BLOCK_PLACE) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(cancelBuild(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent event) {
        if (ServerOptions.BLOCK_BREAK) return;

        event.setCancelled(cancelBuild(event.getPlayer()));
    }

    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {
        event.setCancelled(!ServerOptions.BLOCK_IGNITE);
    }

    @EventHandler
    public void blockExplode(BlockExplodeEvent event) {
        event.setCancelled(!ServerOptions.BLOCK_EXPLODE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void command(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) {
            player.sendMessage("§cOcorreu um problema ao carregar a sua conta! Contate um administrador!");
            event.setCancelled(true);
            return;
        }

        String command = event.getMessage().split(" ")[0];

        UserModel user = UserModel.of(account.getId());

        if (user != null && user.inCombat() && !account.isStaffer()) {
            event.setCancelled(true);

            account.send("§cAguarde " + TimeUtil.formatTime(user.getCombat().getExpiresAt(), TimeUtil.TimeFormat.SHORT) + " para sair de combate!");
            return;
        }

        if (account.hasCooldown("command-cooldown")) {
            event.setCancelled(true);

            account.send("§cAguarde " + account.getFormattedCooldown("command-cooldown") + " para executar comandos novamente.");
            return;
        }

        HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(command);

        if (topic == null) {
            event.setCancelled(true);
            player.sendMessage("§cComando não encontrado.");
        }

        if (!account.hasRank(RankType.VIP))
            account.setCooldown("command-cooldown", TimeUnit.SECONDS.toMillis(3));
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        event.setCancelled(!ServerOptions.DAMAGE_ENABLED);
    }

    @EventHandler
    public void achievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void weather(WeatherChangeEvent event) {
        World world = event.getWorld();

        if (ServerOptions.ALWAYS_DAY) {
            world.setTime(0);
            world.setGameRuleValue("doDaylightCycle", "false");
        }

        if (world.hasStorm())
            world.setWeatherDuration(0);

        event.setCancelled(!ServerOptions.WEATHER_ENABLED);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void food(FoodLevelChangeEvent event) {
        if (!ServerOptions.FOOD_ENABLED)
            event.setFoodLevel(20);

        event.setCancelled(!ServerOptions.FOOD_ENABLED);
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void creature(EntitySpawnEvent event) {
        for (EntityType type : BLOCKED_ENTITIES) {
            if (event.getEntity().getType().equals(type)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!(event.getEntity() instanceof ArmorStand || event.getEntityType().equals(EntityType.DROPPED_ITEM))) {
            event.setCancelled(!ServerOptions.SPAWN_CREATURES);
        }
    }
}