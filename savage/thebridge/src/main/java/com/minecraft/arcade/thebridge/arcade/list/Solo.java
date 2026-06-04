package com.minecraft.arcade.thebridge.arcade.list;

import com.google.common.collect.ImmutableSet;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.team.preset.TeamPreset;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.event.type.player.PlayerCooldownEndEvent;
import com.minecraft.core.bukkit.manager.list.HologramManager;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.member.context.stats.ArcadeStats;
import com.minecraft.core.member.list.thebridge.TheBridgeMember;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import com.minecraft.core.util.list.bukkit.MapUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@IgnoreEvent
public class Solo extends Arcade {

    private final HologramManager hologramManager;

    private final String ARROW_COOLDOWN_KEY = "Flecha",
            PORTAL_IDENTIFIER = "portal:%s"; // portal:red

    private final Set<Material> prohibitedDrops = ImmutableSet.of(
            Material.GOLDEN_APPLE, Material.ARROW, Material.BOW, Material.DIAMOND_PICKAXE,
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
    );

    public Solo(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.THE_BRIDGE_SOLO);

        this.hologramManager = BukkitCore.getManager().getHologram();
    }

    @Override
    public void handleSidebar(User user) {
        Arena arena = user.getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle((arena.isType(com.minecraft.core.arcade.room.type.Type.CASUAL) ? "§6" : arena.isCustom() ? "§b" : "§5") + "§lTHE BRIDGE");
        sidebar.blankRow();

        // Renderizar scoreboard quando o jogo iniciou
        if (arena.isPhase(RoomPhase.PLAYING)) {
            handleTeamStyle(arena, sidebar);

            if (user.isPlayer()) {
                TheBridgeMember member = user.getMember();
                ArcadeStats stats = member.getStats(getCategory());

                sidebar.addRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));
                sidebar.addRow("points", "Pontos: §a" + Util.formatNumber(user.getTeam().getScore()));
                sidebar.blankRow();
            } else {
                sidebar.blankRow();
                sidebar.addRow("state", user.isVanish() ? "§cMODO VANISH" : "§eMODO ESPECTADOR");
            }
        } else {
            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());

            sidebar.blankRow();
            arena.renderSidebarTime(sidebar);

            sidebar.addRow("players", "Jogadores: §a" + arena.getMatchUsers().size() + "/" + arena.getMaxPlayers());

            sidebar.blankRow();
        }

        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    public void handleTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        teamList.forEach(team -> sidebar.addRow(team.getCodeId(), team.getBracketsName() + ": "
                + "§7" + Util.formatNumber(team.getScore())));

        sidebar.blankRow();
    }

    public void updateTeamStyle(Arena arena, Sidebar sidebar) {
        List<TeamPreset> teamList = arena.getTeamList();

        teamList.forEach(team -> sidebar.updateRow(team.getCodeId(), team.getBracketsName() + ": "
                + "§7" + Util.formatNumber(team.getScore())));
    }

    @Override
    public void handleHotbar(Player player, Arena arena) {
        if (isValid(player)) {
            User user = (User) User.of(player.getUniqueId());

            BukkitUtil.sendLeatherArmor(player, user.getTeam().getRgb());

            String base64 = user.getMember().getBase64(ArcadeCategory.THE_BRIDGE_SOLO);

            if (base64 != null)
                Serialization.sendInventoryToPlayerFromBase64WithColor(player, base64, Material.STAINED_CLAY, ColorUtil.getIdByColor(user.getTeam().getColor()));
        } else
            handleDefaultHotbar(player);
    }

    public void handleDefaultHotbar(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        player.getInventory().clear();

        if (user.isPlayer() && arena.isPreGame())
            handleWaitingHotbar(inv);

        if (user.inState(com.minecraft.core.arcade.route.state.ArcadeState.DEAD) || arena.isPhase(RoomPhase.ENDING))
            handleDeathHotbar(inv);

        if (!user.inState(com.minecraft.core.arcade.route.state.ArcadeState.RESPAWN))
            inv.setItem(8, Item.of(Material.ACACIA_DOOR_ITEM, "§cVoltar").interact(event -> event.getPlayer().performCommand("hub")));
    }

    public void handleWaitingHotbar(org.bukkit.inventory.PlayerInventory inv) {
        // Hotbar durante espera
    }

    public void handleDeathHotbar(org.bukkit.inventory.PlayerInventory inv) {
        inv.setItem(0, Item.of(Material.PAPER, "§aJogar Novamente")
                .interact(event -> event.getPlayer().performCommand("playagain")));
    }

    public void start(Player player) {
        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer()) {
            Arena arena = user.getArena();

            TeamPreset team = user.getTeam();

            /* Renderizar hologramas dos portais */

            String teamIdentifier = String.format(PORTAL_IDENTIFIER, team.getName().toLowerCase());

            if (team.hasPortal() && hologramManager.notExistsClient(player, teamIdentifier)) {
                HologramClient yourPortal = hologramManager.spawnClient(player, teamIdentifier, team.getPortalLocation().clone().add(0, 1.8, 0));

                yourPortal.setText(Arrays.asList(
                        team.getColor() + "Sua fortaleza",
                        "§7Defenda a todo custo!"
                ));
            }

            List<TeamPreset> opponents = arena.getTeamList().stream().filter(found -> !found.equals(team) && found.hasPortal()).collect(Collectors.toList());

            opponents.forEach(opponent -> {
                String opponentIdentifier = String.format(PORTAL_IDENTIFIER, opponent.getName().toLowerCase());

                if (hologramManager.notExistsClient(player, opponentIdentifier)) {
                    HologramClient opponentPortal = hologramManager.spawnClient(player, opponentIdentifier, opponent.getPortalLocation().clone().add(0, 1.8, 0));

                    opponentPortal.setText(Arrays.asList(
                            opponent.getColor() + "Ponto de Controle",
                            "§7Pule para marcar!"));
                }
            });
        }
    }

    @Override
    public void handleDeath(User user, User killer, DeathCause cause) {
        super.handleDeath(user, killer, cause);

        if (killer != null) {
            killer.getSidebar().updateRow("kills", "Kills: §a" + Util.formatNumber(killer.getKills()));
        }
    }

    public void handlePoint(User user, Arena arena, DeathCause cause, TeamPreset winner, TeamPreset loser) {
        if (arena.getAliveUsers(loser).size() > 1) {
            user.setState(com.minecraft.core.arcade.route.state.ArcadeState.DEAD);
            sendDeathMessage(arena, user, user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null, cause);
            return;
        }

        com.minecraft.core.account.Account account = user.getAccount();
        Player player = account.player();

        winner.setScore(winner.getScore() + 1);

        net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent("§6§m------------------------------------------");

        message.addExtra("\n\n");

        message.addExtra(com.minecraft.core.util.list.StringUtil.StringHelper.makeCenteredMessage(winner.getColor() + account.getNickname()
                + " §7(§a" + Util.formatNumber(player.getHealth()) + "§c❤§7)§e marcou! §7(§6" + winner.getScore() + "º Ponto§7)" + "\n"));

        message.addExtra(com.minecraft.core.util.list.StringUtil.StringHelper.makeCenteredMessage(winner.getColor().toString() + winner.getScore() + " §7- "
                + loser.getColor().toString() + loser.getScore() + "\n\n"));

        message.addExtra("§6§m------------------------------------------\n\n");

        arena.send(message);

        if (winner.getScore() >= 5) {
            arena.setWinner(winner);
            arena.setPhase(RoomPhase.ENDING);

            loser.getPlayers().forEach(loserPlayer -> {
                User loserUser = (User) User.of(loserPlayer.getUniqueId());

                if (loserUser != null)
                    loserUser.setState(com.minecraft.core.arcade.route.state.ArcadeState.DEAD);
            });
        } else
            arena.setPhase(RoomPhase.RESTARTING);
    }

    public void sendDeathMessage(Arena arena, User user, User killer, DeathCause cause) {
        boolean hasKiller = killer != null;

        if (hasKiller) {
            com.minecraft.core.account.Account account = killer.getAccount();

            account.sound(Sound.SUCCESSFUL_HIT);
        }

        com.minecraft.core.account.Account account = user.getAccount();

        arena.send(user.getTeam().getColor() + account.getNickname() + " §e" +
                (cause.equals(DeathCause.PLAYER) ? (hasKiller ? "foi morto por " + killer.getTeam().getColor() + killer.getAccount().getNickname() : "morreu")
                        : (hasKiller ? "foi jogado no void por " + killer.getTeam().getColor() + killer.getAccount().getNickname() : "caiu no void"))
                + "§e.");
    }


    public boolean isValid(Player player) {
        User user = (User) User.of(player.getUniqueId());

        return user != null && user.isPlayer() && user.inState(com.minecraft.core.arcade.route.state.ArcadeState.ALIVE) && user.getArcade().isCategory(getCategory());
    }


    @EventHandler
    public void onArrowCooldownEnd(PlayerCooldownEndEvent event) {
        Player player = event.getPlayer();

        Cooldown cooldown = event.getCooldown();

        if (isValid(player) && cooldown.getName().equalsIgnoreCase(ARROW_COOLDOWN_KEY)) {
            player.getInventory().addItem(Item.of(Material.ARROW));

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Arrow && projectile.getShooter() instanceof Player) {
            Player shooter = (Player) projectile.getShooter();

            if (isValid(shooter)) {
                User shooterUser = (User) User.of(shooter.getUniqueId());
                Arena arena = shooterUser.getArena();

                if (!arena.isPhase(RoomPhase.PLAYING)) {
                    event.setCancelled(true);
                    return;
                }

                shooter.getInventory().remove(Material.ARROW);

                BukkitCore.getManager().getCooldown().addCooldown(shooter.getUniqueId(), ARROW_COOLDOWN_KEY, 3);
            }
        }
    }

    @EventHandler
    public void onAppleConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItem();

        if (isValid(player) && item != null && item.getType() == Material.GOLDEN_APPLE)
            player.setHealth(player.getMaxHealth());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItemDrop().getItemStack();

        if (isValid(player) && item != null && prohibitedDrops.contains(item.getType()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStainedBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            User user = (User) User.of(player.getUniqueId());
            Arena arena = user.getArena();

            Block block = event.getBlock();

            if (arena.isPhase(RoomPhase.PLAYING) && block.getType().equals(Material.STAINED_CLAY))
                event.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockPortal(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (isValid(player)) {
            User user = (User) User.of(player.getUniqueId());
            Arena arena = user.getArena();

            Block block = event.getBlockPlaced();

            if (arena.isPhase(RoomPhase.PLAYING) && MapUtil.isNearPortal(block.getLocation(), 2))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            Player player = event.getPlayer();

            if (!isValid(player)) return;

            User user = (User) User.of(player.getUniqueId());

            TeamPreset team = user.getTeam();

            Arena arena = user.getArena();

            event.setCancelled(true);

            if (arena.isPhase(RoomPhase.PLAYING)) {
                if (team.hasPortal() && team.getPortalLocation().distance(player.getLocation()) <= 5)
                    return;

                player.playSound(player.getLocation(), Sound.FALL_BIG, 1.0f, 1.0f);

                arena.getTeamList().stream().filter(loser -> !loser.equals(team))
                        .findFirst()
                        .ifPresent(loser -> handlePoint(user, arena, DeathCause.PLAYER, team, loser));
            }
        }
    }

}
