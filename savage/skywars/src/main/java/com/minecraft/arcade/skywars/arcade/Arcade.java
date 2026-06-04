package com.minecraft.arcade.skywars.arcade;

import com.minecraft.arcade.skywars.SkyWars;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.menu.game.play.PlayAgainMenu;
import com.minecraft.arcade.skywars.menu.game.teleport.TeleportMenu;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.redirect.Redirect;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.slime.SlimeWorldController;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.manager.Manager;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.member.list.skywars.objects.metadata.objects.stats.SkyStats;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.TimeUtil;
import lombok.Getter;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public abstract class Arcade extends ArcadeHolder {

    private final File mapsDirectory;

    @Getter
    private final Manager manager;

    public Arcade(String mapsDirectory, Integer minRooms, Integer maxRooms, ArcadeCategory category) {
        super(mapsDirectory, minRooms, maxRooms, category);

        this.mapsDirectory = new File(SkyWars.getInstance().getDataFolder().getAbsolutePath() + "/maps");

        this.manager = BukkitCore.getManager();
    }

    /**
     * Unused method - required by ArcadeHolder
     */
    @Override
    public Room setupRoom(Map map, Slot slot) {
        return null;
    }

    public Arena setupRoom(Map map, Slot slot, Type type) {
        java.time.Instant now = java.time.Instant.now();

        int id = (getId().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        Core.getLogger().info("[" + getName() + "/" + id + "] Iniciando arena...");

        String templateName = getName().toLowerCase() + "-" + map.getName().toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(templateName, worldName, arena, id, now);

        return arena;
    }

    public void copyArena(Arena root) {
        java.time.Instant now = java.time.Instant.now();

        int id = root.getId();

        Map map = root.getMap();

        Arena arena = new Arena(id, this, map, root.getSlot());

        Core.getLogger().info("[" + getName() + "/" + id + "] Copiando arena...");

        String[] mapSplit = map.getName().split(" ");

        String mapName = mapSplit.length > 1 ? mapSplit[0] + "-" + mapSplit[1] : map.getName();

        String templateName = getName().toLowerCase() + "-" + mapName.toLowerCase(),
                worldName = templateName + "-" + id;

        handleArenaData(templateName, worldName, arena, id, now);

    }

    @Override
    public boolean load() {
        Core.getLogger().info("[" + getName() + "] Iniciando jogo...");

        if (!loadMaps(mapsDirectory)) return false;

        java.util.List<Map> availableMaps = getMaps();

        if (availableMaps.isEmpty()) {
            Core.getLogger().warning("[" + getName() + "] Não há mapas disponíveis para este jogo.");
            return false;
        }

        int numRooms = getMinRooms(), casualRooms = (int) Math.floor(numRooms * 0.7);

        java.util.List<Type> availableTypes = new java.util.ArrayList<>();
        for (int i = 0; i < casualRooms; i++) availableTypes.add(Type.CASUAL);

        java.util.Collections.shuffle(availableTypes);

        int mapIndex = 0;
        for (Type type : availableTypes) {
            Map map = availableMaps.get(mapIndex);

            if (setupRoom(map, getCategory().getSlots().get(0), type) == null) return false;

            mapIndex = (mapIndex + 1) % availableMaps.size();
        }

        Core.getLogger().info("[" + getName() + "] Jogo iniciado com sucesso.");
        return true;
    }

    @Override
    public void unload() {
        Core.getLogger().info("[" + getName() + "] Desligando dados...");

        getRooms().clear();

        Core.getLogger().info("[" + getName() + "] O jogo foi interrompido.");
    }

    public void removeArena(Arena root) {
        getRooms().remove(root);
    }

    protected void handleArenaData(String template, String world, Arena arena, int id, Instant now) {
        ArcadeCategory arcade = arena.getArcade().getCategory();
        Slot slot = arena.getSlot();

        SlimeWorldController.cloneWorldFromTemplate(SWMPlugin.getInstance(),
                template,
                world, () -> {
                    int minPlayers = !arcade.name().contains("VERSUS")
                            ? slot.equals(Slot.SOLO) ? 4 : slot.equals(Slot.DUO) ? 8 : slot.equals(Slot.TRIO) ? 6 : 12
                            : slot.getMaxPlayers();

                    int maxPlayers = !arcade.name().contains("VERSUS")
                            ? slot.equals(Slot.DUO) ? 16 : slot.equals(Slot.TRIO) ? 12 : slot.equals(Slot.QUARTET) ? 16 : 8
                            : slot.getMaxPlayers();

                    arena.setMinPlayers(minPlayers);
                    arena.setMaxPlayers(maxPlayers);

                    arena.handleBorder();

                    getRooms().add(arena);

                    Core.getLogger().info("[" + getName() + "/" + id + "] Arena iniciada com sucesso.");
                });
    }

    @Override
    public Arena findBestArena(UUID sender, ArcadeRouteContext route) {
        Account account = Core.getAccountData().of(sender);
        Redirect redirect = Core.getRedirectData().of(sender);

        if (redirect != null) {
            return findRedirectedArena(route);
        }

        Stream<Arena> stream = getFilteredArenas(route);

        return stream.filter(arena -> isArenaValidForPlayer(arena, route, account, sender))
                .findFirst()
                .orElse(null);
    }

    private Arena findRedirectedArena(ArcadeRouteContext route) {
        return getRooms().stream()
                .filter(room -> room.isCategory(route.getArcade())
                        && room.getId() == route.getRoomId()
                        && room.isFiltered(route.getMapId()))
                .map(room -> (Arena) room)
                .findFirst()
                .orElse(null);
    }

    private Stream<Arena> getFilteredArenas(ArcadeRouteContext route) {
        Stream<Arena> stream = getRooms().stream()
                .map(room -> (Arena) room)
                .filter(arena -> arena.getSlot().equals(route.getSlot()));

        if (route.getType() != null) {
            stream = stream.filter(arena -> arena.getType().equals(route.getType()));
        }

        if (route.hasMap()) {
            stream = stream.filter(arena -> arena.isFiltered(route.getMapId()));
        }

        return stream;
    }

    private boolean isArenaValidForPlayer(Arena arena, ArcadeRouteContext route, Account account, UUID sender) {
        System.out.println("[Search] Filtrando " + arena.getIdentifier() + " para " + sender);

        if (account.hasReconnect() && arena.getId() == route.getRoomId() && arena.getMap().getId() == route.getMapId()) {
            return true;
        }

        if (!arena.isAvailable()) {
            return false;
        }

        if (account.isPartyOwner() && route.isLinked() && !arena.hasReservations(route.getLink())) {
            arena.setReservations(route.getLink());
            System.out.println("[PT-Route] " + arena.getIdentifier() + " recebeu +" + route.getLink().size() + " reservas. " +
                    "Total de reservas: " + arena.getTotalReservations());
        }

        if (account.isPartyOwner() && arena.isReserved() && arena.hasReservation(sender)) {
            Core.getLogger().info("[PT-Route] O dono " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
            return true;
        }

        if (arena.isReserved() && arena.hasReservation(sender) && !arena.isFull()) {
            arena.removeReservation(sender);
            System.out.println("[PT-Route] " + account.getNickname() + " entrou em " + arena.getIdentifier() + ".");
            return true;
        }

        return arena.isAvailable();
    }

    public void handleDeath(Arena arena, User user, User killer, DeathCause cause) {
        org.bukkit.entity.Player player = user.getAccount().player();

        boolean hasKiller = killer != null;
        player.setHealth(player.getMaxHealth());
        // Remove all potion effects
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        BukkitCore.getManager().getCooldown().resetCooldown(player);

        user.getContext().setAlive(false);
        user.setCombat(null);

        if (hasKiller) {
            killer.setCombat(null);
            killer.getContext().setKills(killer.getContext().getKills() + 1);

            // Atualizar kills nas stats
            SkyStats killerStats = killer.getMember().getStats(arena.getArcade().getCategory(), arena.getType());
            if (killerStats != null) {
                killerStats.setKills();
                killer.getMember().updateStats(killerStats);
            }

            killer.getAccount().send("§a+" + killer.getContext().getKills() + " Kill!");
        }

        user.setState(ArcadeState.DEAD);

        user.setTag(Tag.SPECTATOR);
        TagManager.updateTag(user.getAccount());

        handleHotbar(user);

        player.playSound(player.getLocation(), org.bukkit.Sound.BLAZE_DEATH, 1.0f, 1.0f);

        sendDeathMessage(arena, user, killer, cause);

        /* Alterar Visibilidade */
        arena.hideAndShow(user);

        // Verificar se há apenas 1 jogador vivo (vencedor)
        List<User> aliveUsers = arena.getUsers().stream()
                .filter(u -> u.getContext().isAlive() && u.inState(ArcadeState.ALIVE))
                .collect(java.util.stream.Collectors.toList());

        if (aliveUsers.size() == 1) {
            User winner = aliveUsers.get(0);
            arena.setWinner(winner);
            arena.setPhase(RoomPhase.ENDING);
        } else if (aliveUsers.isEmpty()) {
            arena.setPhase(RoomPhase.ENDING);
        }
    }

    private void sendDeathMessage(Arena arena, User user, User killer, DeathCause cause) {
        Account account = user.getAccount();
        String message;

        if (killer != null) {
            Account killerAccount = killer.getAccount();
            message = account.getRank().getColor() + account.getNickname() + " §efoi morto por " + killerAccount.getRank().getColor() + killerAccount.getNickname() + "§e.";
        } else if (cause == DeathCause.VOID) {
            message = account.getRank().getColor() + account.getNickname() + " §emorreu no vazio.";
        } else {
            message = account.getRank().getColor() + account.getNickname() + " §emorreu.";
        }

        arena.send(message);
    }

    public enum DeathCause {
        PLAYER, VOID, NONE
    }

    public void load(User user) {
        if (!user.isPlayer()) return;
        
        org.bukkit.entity.Player player = user.getAccount().player();
        if (player == null || !player.isOnline()) return;

        handleSidebar(user);
        handleHotbar(user);
    }

    public void handleSidebar(User user) {
        Arena arena = user.getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle((arena.isType(Type.CASUAL) ? "§b" : "§5") + "§lSKY WARS");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + arena.getIdentifier());
        sidebar.blankRow();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            if (arena.hasProperty("refill")) {
                int refillTime = (int) arena.getProperty("refill");
                sidebar.addRow("event_title", "§ePróximo evento:");
                sidebar.addRow("refill", " Refill: §a" + TimeUtil.time(refillTime));
                sidebar.blankRow();
            }
            
            sidebar.addRow("kills", "Kills: §a" + Util.formatNumber(user.getKills()));
            
            long aliveCount = arena.getAliveUsers().size();
            sidebar.addRow("alive", "Vivos: §a" + aliveCount);
            
            sidebar.blankRow();
            sidebar.addRow("time", "Tempo: §a" + TimeUtil.time(arena.getFullTime()));
        } else {
            sidebar.addRow("map", "Mapa: §a" + arena.getMap().getName());
            sidebar.addRow("mode", "Modo: §a" + arena.getArcade().getCategory().getName());
            sidebar.blankRow();
            arena.renderSidebarTime(sidebar);
            sidebar.addRow("players", "Jogadores: §a" + arena.getTotalPlayers() + "/" + arena.getMaxPlayers());
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    public void handleHotbar(User user) {
        org.bukkit.entity.Player player = user.getAccount().player();
        
        if (player == null) return;

        Arena arena = user.getArena();

        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.getInventory().clear();

            if (user.isPlayer()) {
                sendDeathHotbar(player, arena);
            }
        } else {
            if (user.isPlayer() && arena.isPreGame()) {
                player.getInventory().clear();

                inv.setItem(0, new Item(org.bukkit.Material.BOW)
                        .name("§aMenu do SkyWars")
                        .interact(event -> event.getPlayer().sendMessage("§cMenu em desenvolvimento!")));

                inv.setItem(8, Item.of(org.bukkit.Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                        .interact(event -> event.getPlayer().performCommand("lobby")));
            } else if (arena.isPhase(RoomPhase.ENDING)) {
                sendDeathHotbar(player, arena);
            }
        }
    }

    protected void sendDeathHotbar(org.bukkit.entity.Player player, Arena arena) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        if (arena.isPhase(RoomPhase.PLAYING))
            inv.setItem(0, Item.of(org.bukkit.Material.COMPASS, "§aTeleportador")
                    .interact(event -> new TeleportMenu(event.getPlayer()).handle()));

        inv.setItem(7, Item.of(org.bukkit.Material.PAPER, "§aJogar novamente")
                .interact(event -> new PlayAgainMenu(event.getPlayer()).handle()));

        inv.setItem(8, Item.of(org.bukkit.Material.ACACIA_DOOR_ITEM, "§cVoltar ao lobby")
                .interact(event -> event.getPlayer().performCommand("lobby")));
    }
}

