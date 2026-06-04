package com.minecraft.lobby.architect;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.tag.role.TagRole;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.map.location.SignedLocation;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.action.NpcAction;
import com.minecraft.core.bukkit.api.npc.type.client.NpcClient;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.menu.account.AccountMenu;
import com.minecraft.core.bukkit.menu.account.stats.StatsInfoMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.stats.BedWarsStatsMenu;
import com.minecraft.core.bukkit.menu.server.collectible.CollectibleMenu;
import com.minecraft.core.event.IgnoreEvent;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.core.util.list.WorldUtil;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.architect.list.main.MainArchitect;
import com.minecraft.lobby.menu.arcade.ArcadeSlotMenu;
import com.minecraft.lobby.menu.navigation.ServersMenu;
import com.minecraft.lobby.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@IgnoreEvent
public abstract class Architect implements Listener {

    private final String id = StringUtil.generateLetterCode(4) + Core.getServerId();
    private final ServerType server;

    private final List<SignedLocation> locations = new ArrayList<>();

    private static final String JOIN_FRAME = "§4§m――§c§m――§6§m――§e§m――§a§m――§b§m――§9§m――";

    private static final List<String> JOIN_MESSAGES = Arrays.asList(
            "§6adentrou no lobby!",
            "§6caiu de paraquedas!",
            "§6chegou surfando!",
            "§6chegou correndo!",
            "§6veio voando!",
            "§6veio de outro multiverso!",
            "§6WOW! Ele veio mesmo!"
    );

    public void load() {
        // Carregar cosméticos no lobby (usado pelo item "Cosméticos" da hotbar)
        Core.getCollectibleController().handle(Lobby.getInstance(), "com.minecraft.core.api.collectible");

        handleEntities();

        WorldUtil.setup(getWorld());

        Bukkit.getPluginManager().registerEvents(this, Lobby.getInstance());
    }

    public void unload() {

    }

    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    public void join(Player player) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        handleSidebar(user);
        handleHotbar(player);

        // Teleportar para o spawn
        Location spawn = getLocation("spawn");
        if (spawn != null) {
            player.teleport(spawn);
        } else {
            // Se não houver spawn configurado, usar o spawn padrão do mundo
            player.teleport(player.getWorld().getSpawnLocation());
        }

        // Garantir que o player não esteja voando
        player.setAllowFlight(false);
        player.setFlying(false);

        if (account.hasRank(RankType.SAVAGE)) {
            String message = JOIN_MESSAGES.get(Core.RANDOM.nextInt(JOIN_MESSAGES.size()));

            Core.getAccountController().list().forEach(target -> {
                String rankNick = account.hasOnlyRank(RankType.VERMELHO)
                        ? "§c§o" + account.getNickname()
                        : account.getTag().getByPrefix(target.getTagPrefix()) + account.getNickname();

                target.send(JOIN_FRAME);
                target.send("");
                target.send(rankNick + " " + message);
                target.send("");
                target.send(JOIN_FRAME);
            });
        }

        /* Esconder jogadores baseado no modo de visibilidade */
        if (user != null) {
            com.minecraft.lobby.user.visibility.PlayerVisibilityMode visMode = user.getVisibilityMode();
            
            Bukkit.getOnlinePlayers().forEach(online -> {
                if (online.equals(player)) return;
                
                switch (visMode) {
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
        }

        Location location = getLocation("npc_stats");

        /* Renderizar NPC de suas estatísticas */
        if (location != null) {
            CompletableFuture.runAsync(() -> {
                NpcClient stats = BukkitCore.getManager().getNpc().spawnClient(player, location,
                        account.getSkin().getValue(), account.getSkin().getSignature());

                stats.setContact(true);
                stats.setAction((target, action) -> {
                    Account other = Core.getAccountController().of(target.getUniqueId());

                    if (other != null) {
                        ServerType server = getServer().getRealServerOfLobby();

                        if (server == null) return;

                        if (server.equals(ServerType.BEDWARS))
                            new BedWarsStatsMenu(target, null).handle();
                        else
                            new StatsInfoMenu(target, other, server, null).handle();
                    }
                });

                stats.display();
            });
        }
    }

    public void quit(Player player) {
    }

    public abstract void handleSidebar(User user);

    public void chat(Player player, String message) {

    }

    public void handleEntities() {
    }

    public Vector getSlimeJump(Vector direction) {
        return new Vector(0, 0, 0);
    }

    public Location getLocation(String name) {
        return locations.stream()
                .filter(location -> location.getName().equalsIgnoreCase(name))
                .map(location -> location.getSynthetic().of(Bukkit.getWorlds().get(0)))
                .findFirst()
                .orElse(null);
    }

    public List<SignedLocation> getLocationsByName(String name) {
        return locations.stream()
                .filter(location -> location.getName().startsWith(name))
                .collect(Collectors.toList());
    }

    public List<Location> getLocationsByKey(String... names) {
        List<Location> list = new ArrayList<>();

        for (String name : names) {
            Location location = getLocation(name);

            if (location != null)
                list.add(location);
        }

        return list;
    }

    public void handleHotbar(Player player) {
        // Usar runTaskLater para garantir que o inventário esteja totalmente carregado
        Core.getPlatform().runSync(() -> {
            if (!player.isOnline()) return;
            
            User user = (User) User.of(player.getUniqueId());
            if (user == null) return;

            Account account = user.getAccount();
            if (account == null) return;
            
            Toggle toggle = account.getToggle();

            PlayerInventory inv = player.getInventory();

            inv.clear();
            inv.setArmorContents(null);

            inv.setItem(0, Item.of(Material.COMPASS, "§aModos de jogo")
                    .interact(event -> new ServersMenu(event.getPlayer(), ServersMenu.NavigationType.SERVER).handle()));

            inv.setItem(1, Item.of(Material.SKULL_ITEM, 3, "§aSua conta")
                    .skullByBase64(account.getSkin().getValue())
                    .interact(event -> new AccountMenu(event.getPlayer()).handle()));

            inv.setItem(4, Item.of(Material.CHEST, "§aCosméticos")
                    .interact(event -> new CollectibleMenu(event.getPlayer()).handle()));

            // Sistema de visibilidade de jogadores com 3 modos
            com.minecraft.lobby.user.visibility.PlayerVisibilityMode visMode = user != null ? 
                    user.getVisibilityMode() : com.minecraft.lobby.user.visibility.PlayerVisibilityMode.ALL;
            
            inv.setItem(7, Item.of(visMode.getMaterial(), visMode.getData(), visMode.getDisplayName()));

            inv.setItem(8, Item.of(Material.NETHER_STAR, "§aLobbies")
                    .interact(event -> new ServersMenu(event.getPlayer(), ServersMenu.NavigationType.ROOM).handle()));
            
            player.updateInventory();
        }, 2L);
    }

    public void handleStatsNpc(Location location) {
        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer("stats_" + getServer().name().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§6§lSUAS ESTATÍSTICAS",
                "§eClique para ver"
        ));
    }

    public NpcServer handleNpc(String name, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(name.toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§b" + name,
                "§e" + Util.formatNumber(0) + " jogando!"
        ));

        return npc;
    }

    public void handleServerNpc(ServerType server, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setAction((player, action) -> {
            User user = (User) User.of(player.getUniqueId());

            if (user != null)
                user.getAccount().redirect(server);
        });

        npc.setContact(true);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(server.name().toLowerCase(), location);

        if (server.getServerLobby() == ServerType.HUB_BEDWARS) {
            hologram.setText(Arrays.asList(
                    MainArchitect.generateColoredText(),
                    "§b" + server.getName(),
                    "§e" + Util.formatNumber(Core.getServerData().getOnlinePlayers(server)) + " jogando!"
            ));
        } else {
            hologram.setText(Arrays.asList(
                    "§b" + server.getName(),
                    "§e" + Util.formatNumber(Core.getServerData().getOnlinePlayers(server)) + " jogando!"
            ));
        }
    }

    public void handleArcadeNpc(ArcadeCategory arcade, Location location, NpcAction action, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);
        npc.setAction(action);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + (arcade.equals(ArcadeCategory.NONE) ? "§k?????" : arcade.getName().toUpperCase()),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        handleArcadeNpc(arcade, location, (player, action) -> new ArcadeSlotMenu(player, null, arcade).handle(), value, signature);
    }
}