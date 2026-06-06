package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.network.NetworkStateCache;
import com.minecraft.lobbyhost.network.QueueManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.minecraft.lobbyhost.message.MenuTexts;
import com.minecraft.lobbyhost.util.BungeeCordUtil;
import com.minecraft.lobbyhost.util.ModernText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menus da hotbar do hub (bussola, perfil, cosmeticos, missoes).
 */
public final class LobbyMenus {

    private static final NamespacedKey MENU_SERVER_KEY =
            new NamespacedKey(LobbyHostPlugin.getInstance(), "menu_server");
    private static final NamespacedKey MENU_MODE_KEY =
            new NamespacedKey(LobbyHostPlugin.getInstance(), "menu_mode");

    private static final int NAV_SIZE = 27;
    private static final int CLOSE_SLOT = 22;

    private LobbyMenus() {}

    public static void openNavigation(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(LobbyHostPlugin.getInstance(), () -> {
            Map<String, Integer> liveCounts = Core.getNetworkPresenceData() != null
                    ? Core.getNetworkPresenceData().countByServer() : Map.of();
            Bukkit.getScheduler().runTask(LobbyHostPlugin.getInstance(), () -> {
                List<NavEntry> entries = buildNavigationEntries(player, liveCounts);
                LobbyMenuHolder holder = new LobbyMenuHolder("navigation");
                Inventory inv = Bukkit.createInventory(holder, NAV_SIZE,
                        MenuTexts.title(player, "menus.lobby.nav-title"));
                holder.bind(inv);
                fillGlass(inv, entries);
                for (NavEntry entry : entries) {
                    if (entry.slot() >= 0 && entry.slot() < NAV_SIZE && entry.slot() != CLOSE_SLOT) {
                        inv.setItem(entry.slot(), navIcon(player, entry));
                    }
                }
                inv.setItem(CLOSE_SLOT, labeledMini(player, Material.BARRIER,
                        MenuTexts.mini(player, "menus.common.close"), List.of()));
                player.openInventory(inv);
            });
        });
    }

    public static void openProfile(Player player) {
        var account = Core.getAccountController().of(player.getUniqueId());
        LobbyMenuHolder holder = new LobbyMenuHolder("profile");
        Inventory inv = Bukkit.createInventory(holder, 27,
                MenuTexts.title(player, "menus.lobby.profile-title"));
        holder.bind(inv);

        List<String> lore = new ArrayList<>();
        if (account != null) {
            lore.add(MenuTexts.mini(player, "menus.lobby.rank-line",
                    MenuTexts.map("rank", account.getRank() != null ? account.getRank().getType().name() : "-")));
            lore.add(MenuTexts.mini(player, "menus.lobby.tag-line",
                    MenuTexts.map("tag", account.getTag() != null ? account.getTag().name() : "-")));
            lore.add(MenuTexts.mini(player, "menus.lobby.medal-line",
                    MenuTexts.map("medal", account.getMedal() != null ? account.getMedal().name() : "-")));
            lore.add(MenuTexts.mini(player, "menus.lobby.friends-count",
                    MenuTexts.map("count", String.valueOf(account.getFriendMetadata() != null
                            ? account.getFriendMetadata().getFriends().size() : 0))));
            int friendsHere = com.minecraft.lobbyhost.social.FriendService.countFriendsOnlineInLobby(account);
            if (friendsHere > 0) {
                lore.add(MenuTexts.mini(player, "menus.lobby.friends-here",
                        MenuTexts.map("count", String.valueOf(friendsHere))));
            }
        } else {
            lore.add(MenuTexts.mini(player, "menus.lobby.account-error"));
        }
        lore.add(ModernText.hint("amigo") + " <#64748B>· " + ModernText.hint("prefs"));

        inv.setItem(10, labeledMini(player, Material.PLAYER_HEAD,
                ModernText.gradient(player.getName(), ModernText.ACCENT_A, ModernText.ACCENT_B), lore));
        inv.setItem(12, labeledMini(player, Material.SUNFLOWER,
                MenuTexts.mini(player, "menus.lobby.friends"),
                List.of(ModernText.action("Clique para abrir"), ModernText.hint("amigo add <nick>"))));
        inv.setItem(14, labeledMini(player, Material.GOLD_BLOCK,
                MenuTexts.mini(player, "menus.lobby.rankings"),
                List.of(MenuTexts.mini(player, "menus.lobby.rankings-lore"), ModernText.hint("leaderboard"))));
        inv.setItem(13, labeledMini(player, Material.COMPARATOR,
                MenuTexts.mini(player, "menus.lobby.prefs"),
                List.of(MenuTexts.mini(player, "menus.lobby.prefs-lore"), ModernText.hint("prefs"))));
        inv.setItem(15, labeledMini(player, Material.REDSTONE,
                MenuTexts.mini(player, "menus.lobby.report"),
                List.of(MenuTexts.mini(player, "menus.lobby.report-lore"), ModernText.hint("report"))));
        inv.setItem(22, labeledMini(player, Material.BARRIER,
                MenuTexts.mini(player, "menus.common.close"), List.of()));
        player.openInventory(inv);
    }

    public static void openCosmetics(Player player) {
        CosmeticMenus.openMainMenu(player);
    }

    public static void openMissions(Player player) {
        LobbyMenuHolder holder = new LobbyMenuHolder("missions");
        Inventory inv = Bukkit.createInventory(holder, 27,
                MenuTexts.title(player, "menus.lobby.missions-title"));
        holder.bind(inv);
        inv.setItem(13, labeledMini(player, Material.CHEST,
                MenuTexts.mini(player, "menus.lobby.missions-soon"),
                List.of(MenuTexts.mini(player, "menus.lobby.missions-soon-lore"))));
        inv.setItem(22, labeledMini(player, Material.BARRIER,
                MenuTexts.mini(player, "menus.common.close"), List.of()));
        player.openInventory(inv);
    }

    public static void handleNavigationClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.getPersistentDataContainer().has(
                new NamespacedKey(LobbyHostPlugin.getInstance(), "nav_blocked"),
                PersistentDataType.BYTE)) {
            player.sendMessage(ModernText.parse(MenuTexts.mini(player, "menus.lobby.mode-unavailable")));
            return;
        }
        String modeId = meta.getPersistentDataContainer().get(MENU_MODE_KEY, PersistentDataType.STRING);
        if (modeId == null || modeId.isBlank()) {
            modeId = meta.getPersistentDataContainer().get(MENU_SERVER_KEY, PersistentDataType.STRING);
        }
        if (modeId == null || modeId.isBlank()) return;
        joinQueue(player, modeId);
    }

    /** Entra na fila de conexão do modo escolhido (bússola / menu). */
    public static void joinQueue(Player player, String modeId) {
        LobbySettings.NetworkMode mode = QueueManager.findMode(modeId);
        if (mode == null) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (NetworkStateCache.isMaintenance()
                && !NetworkStateCache.canJoinDuringMaintenance(account)) {
            player.sendMessage(ModernText.parse(MenuTexts.mini(player, "menus.lobby.maintenance-join")));
            return;
        }

        player.closeInventory();
        QueueManager.join(player, mode.id());
    }

    public static void handleProfileClick(Player player, int slot) {
        switch (slot) {
            case 12 -> FriendMenu.open(player);
            case 14 -> new com.minecraft.lobbyhost.bukkit.menu.leaderboard.LeaderboardHubMenu(player).open();
            case 13 -> {
                Account account = Core.getAccountController().of(player.getUniqueId());
                if (account != null) new com.minecraft.lobbyhost.bukkit.menu.prefs.PrefsMenu(player, account).open();
            }
            case 15 -> {
                Account account = Core.getAccountController().of(player.getUniqueId());
                if (account != null) com.minecraft.lobbyhost.bukkit.menu.report.ReportMenus.open(player, account);
            }
            default -> {}
        }
    }

    public static void connectBungee(Player player, String server) {
        LobbyHostPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (NetworkStateCache.isMaintenance()
                && !NetworkStateCache.canJoinDuringMaintenance(account)) {
            player.sendMessage(ModernText.parse(MenuTexts.mini(player, "menus.lobby.maintenance-join")));
            return;
        }
        player.closeInventory();
        BungeeCordUtil.connect(player, plugin, server);
        player.sendMessage(ModernText.parse(MenuTexts.format(player, "menus.lobby.connecting",
                MenuTexts.map("server", server))));
    }

    /** Sem heartbeat no Redis = proxy/game nao publicou online (TTL 30s). Zero jogadores nao bloqueia. */
    private static boolean isServerOffline(String heartbeatId) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return false;
        String raw = Core.getRedis().get(Constant.REDIS_NETWORK_ONLINE_KEY_PREFIX + heartbeatId);
        return raw == null || raw.isBlank();
    }

    private static List<NavEntry> buildNavigationEntries(Player player, Map<String, Integer> liveCounts) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        boolean blockJoin = NetworkStateCache.isMaintenance()
                && !NetworkStateCache.canJoinDuringMaintenance(account);

        List<NavEntry> list = new ArrayList<>();
        for (LobbySettings.NetworkMode mode : LobbySettings.NETWORK_MODES) {
            int redisOnline = NetworkStateCache.getOnline(mode.heartbeatId());
            int liveOnline = liveCounts.getOrDefault(mode.bungeeServer(), 0);
            int online = Math.max(redisOnline, liveOnline);
            boolean serverOffline = isServerOffline(mode.heartbeatId());
            boolean unavailable = blockJoin || serverOffline;
            List<String> lore = new ArrayList<>();
            if (blockJoin) {
                lore.add(MenuTexts.mini(player, "menus.lobby.maintenance"));
                lore.add(MenuTexts.mini(player, "menus.lobby.maintenance-lore"));
                lore.add(MenuTexts.mini(player, "menus.lobby.maintenance-staff"));
            } else if (serverOffline) {
                lore.add(MenuTexts.mini(player, "menus.lobby.offline"));
                lore.add(MenuTexts.mini(player, "menus.lobby.offline-lore"));
            } else {
                lore.add(MenuTexts.mini(player, "menus.lobby.players-online",
                        MenuTexts.map("count", String.valueOf(Math.max(online, 0)))));
                lore.add("");
                lore.add(MenuTexts.mini(player, "menus.lobby.click-queue"));
            }
            list.add(new NavEntry(mode.id(), mode.bungeeServer(), mode.displayName(),
                    unavailable ? Material.GRAY_STAINED_GLASS_PANE : mode.material(),
                    mode.slot(), lore, unavailable));
        }
        return list;
    }

    private static void fillGlass(Inventory inv, List<NavEntry> entries) {
        ItemStack glass = glassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            if (i == CLOSE_SLOT) continue;
            boolean reserved = false;
            for (NavEntry e : entries) {
                if (e.slot() == i) {
                    reserved = true;
                    break;
                }
            }
            if (!reserved) inv.setItem(i, glass);
        }
    }

    private static ItemStack glassPane() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.noItalic(ModernText.parse("<black>")));
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack navIcon(Player player, NavEntry entry) {
        ItemStack stack = labeledMini(player, entry.material(), entry.displayName(), entry.lore());
        ItemMeta meta = stack.getItemMeta();
        if (entry.blocked()) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(LobbyHostPlugin.getInstance(), "nav_blocked"),
                    PersistentDataType.BYTE, (byte) 1);
        } else {
            meta.getPersistentDataContainer().set(MENU_MODE_KEY, PersistentDataType.STRING, entry.id());
            meta.getPersistentDataContainer().set(MENU_SERVER_KEY, PersistentDataType.STRING, entry.server());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack labeledMini(Player player, Material material, String miniName, List<String> miniLore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.noItalic(ModernText.parse(miniName)));
        if (miniLore != null && !miniLore.isEmpty()) {
            meta.lore(miniLore.stream()
                    .map(l -> ModernText.noItalic(ModernText.parse(l)))
                    .toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private record NavEntry(String id, String server, String displayName, Material material,
                            int slot, List<String> lore, boolean blocked) {}
}
