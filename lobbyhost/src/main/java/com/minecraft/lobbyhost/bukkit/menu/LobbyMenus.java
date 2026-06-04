package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.network.NetworkStateCache;
import com.minecraft.lobbyhost.social.PartyService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.minecraft.lobbyhost.util.BungeeCordUtil;
import com.minecraft.lobbyhost.util.ModernText;

import java.util.ArrayList;
import java.util.List;

/**
 * Menus da hotbar do hub (bussola, perfil, cosmeticos, missoes).
 */
public final class LobbyMenus {

    private static final NamespacedKey MENU_SERVER_KEY =
            new NamespacedKey(LobbyHostPlugin.getInstance(), "menu_server");

    private static final int NAV_SIZE = 27;
    private static final int CLOSE_SLOT = 22;

    private LobbyMenus() {}

    public static void openNavigation(Player player) {
        List<NavEntry> entries = buildNavigationEntries(player);
        LobbyMenuHolder holder = new LobbyMenuHolder("navigation");
        Inventory inv = Bukkit.createInventory(holder, NAV_SIZE,
                ModernText.noItalic(ModernText.parse(LobbySettings.NAVIGATION_TITLE_MINI)));
        holder.bind(inv);

        fillGlass(inv, entries);

        for (NavEntry entry : entries) {
            if (entry.slot() >= 0 && entry.slot() < NAV_SIZE && entry.slot() != CLOSE_SLOT) {
                inv.setItem(entry.slot(), navIcon(entry));
            }
        }

        inv.setItem(CLOSE_SLOT, labeledMini(Material.BARRIER,
                "<!italic><" + ModernText.ERROR + "><bold>Fechar</bold>", List.of()));
        player.openInventory(inv);
    }

    public static void openProfile(Player player) {
        var account = Core.getAccountController().of(player.getUniqueId());
        LobbyMenuHolder holder = new LobbyMenuHolder("profile");
        Inventory inv = Bukkit.createInventory(holder, 27,
                ModernText.noItalic(ModernText.parse(
                        ModernText.gradient("Seu perfil", ModernText.ACCENT_A, ModernText.ACCENT_B))));
        holder.bind(inv);

        List<String> lore = new ArrayList<>();
        if (account != null) {
            lore.add(ModernText.mini("Rank: " + (account.getRank() != null ? account.getRank().getType().name() : "-")));
            lore.add(ModernText.mini("Tag: " + (account.getTag() != null ? account.getTag().name() : "-")));
            lore.add(ModernText.mini("Medalha: " + (account.getMedal() != null ? account.getMedal().name() : "-")));
            lore.add(ModernText.mini("Amigos: " + (account.getFriendMetadata() != null
                    ? account.getFriendMetadata().getFriends().size() : 0)));
            int friendsHere = com.minecraft.lobbyhost.social.FriendService.countFriendsOnlineInLobby(account);
            if (friendsHere > 0) {
                lore.add("<!italic><" + ModernText.SUCCESS + ">Amigos no lobby: " + friendsHere);
            }
        } else {
            lore.add("<!italic><" + ModernText.ERROR + ">Conta não carregada");
        }
        lore.add(ModernText.hint("amigo") + " <#64748B>· " + ModernText.hint("prefs"));

        inv.setItem(10, labeledMini(Material.PLAYER_HEAD,
                ModernText.gradient(player.getName(), ModernText.ACCENT_A, ModernText.ACCENT_B), lore));
        inv.setItem(12, labeledMini(Material.SUNFLOWER,
                ModernText.brandGradient("Amigos"),
                List.of(ModernText.action("Clique para abrir"), ModernText.hint("amigo add <nick>"))));
        inv.setItem(14, labeledMini(Material.IRON_INGOT,
                ModernText.gradient("Party", "#C084FC", "#F472B6"),
                List.of(ModernText.hint("party criar"), ModernText.hint("party convidar <nick>"))));
        inv.setItem(16, labeledMini(Material.GOLD_BLOCK,
                ModernText.brandGradient("Rankings"),
                List.of(ModernText.mini("Kills, money, clãs…"), ModernText.hint("leaderboard"))));
        inv.setItem(13, labeledMini(Material.COMPARATOR,
                ModernText.gradient("Preferências", ModernText.WARN, ModernText.BRAND_B),
                List.of(ModernText.mini("Social · anticheat · staff"), ModernText.hint("prefs"))));
        inv.setItem(15, labeledMini(Material.REDSTONE,
                ModernText.gradient("Denunciar", ModernText.ERROR, "#FCA5A5"),
                List.of(ModernText.mini("Reportar jogador"), ModernText.hint("report"))));
        inv.setItem(22, labeledMini(Material.BARRIER,
                "<!italic><" + ModernText.ERROR + "><bold>Fechar</bold>", List.of()));
        player.openInventory(inv);
    }

    public static void openCosmetics(Player player) {
        CosmeticMenus.openMainMenu(player);
    }

    public static void openMissions(Player player) {
        LobbyMenuHolder holder = new LobbyMenuHolder("missions");
        Inventory inv = Bukkit.createInventory(holder, 27,
                ModernText.noItalic(ModernText.parse(ModernText.brandGradient("Missões"))));
        holder.bind(inv);
        inv.setItem(13, labeledMini(Material.CHEST,
                ModernText.brandGradient("Em breve"),
                List.of(ModernText.mini("Missões diárias em desenvolvimento"))));
        inv.setItem(22, labeledMini(Material.BARRIER,
                "<!italic><" + ModernText.ERROR + "><bold>Fechar</bold>", List.of()));
        player.openInventory(inv);
    }

    public static void handleNavigationClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.getPersistentDataContainer().has(
                new NamespacedKey(LobbyHostPlugin.getInstance(), "nav_blocked"),
                PersistentDataType.BYTE)) {
            player.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                    + ">Este modo está indisponível no momento."));
            return;
        }
        String server = meta.getPersistentDataContainer()
                .get(MENU_SERVER_KEY, PersistentDataType.STRING);
        if (server == null || server.isBlank()) return;
        connectBungee(player, server);
    }

    public static void handleProfileClick(Player player, int slot) {
        switch (slot) {
            case 12 -> FriendMenu.open(player);
            case 14 -> player.performCommand("party info");
            case 16 -> new com.minecraft.lobbyhost.bukkit.menu.leaderboard.LeaderboardHubMenu(player).open();
            case 13 -> {
                Account account = Core.getAccountController().of(player.getUniqueId());
                if (account != null) new com.minecraft.lobbyhost.bukkit.menu.prefs.PrefsMenu(player, account).open();
            }
            case 15 -> {
                Account account = Core.getAccountController().of(player.getUniqueId());
                if (account != null) new com.minecraft.lobbyhost.bukkit.menu.ReportMenu(player, account).open();
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
            player.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                    + ">Rede em manutenção. Apenas staff autorizado pode entrar."));
            return;
        }
        player.closeInventory();
        if (PartyService.inParty(player.getUniqueId())
                && player.getUniqueId().equals(PartyService.leaderOf(player.getUniqueId()))) {
            PartyService.connectParty(player, server);
        } else {
            BungeeCordUtil.connect(player, plugin, server);
        }
        player.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS + ">Conectando em <#F8FAFC>"
                + server + "<" + ModernText.SUCCESS + ">…"));
    }

    /** Sem heartbeat no Redis = proxy/game nao publicou online (TTL 30s). Zero jogadores nao bloqueia. */
    private static boolean isServerOffline(String heartbeatId) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return false;
        String raw = Core.getRedis().get(Constant.REDIS_NETWORK_ONLINE_KEY_PREFIX + heartbeatId);
        return raw == null || raw.isBlank();
    }

    private static List<NavEntry> buildNavigationEntries(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        boolean blockJoin = NetworkStateCache.isMaintenance()
                && !NetworkStateCache.canJoinDuringMaintenance(account);

        List<NavEntry> list = new ArrayList<>();
        for (LobbySettings.NetworkMode mode : LobbySettings.NETWORK_MODES) {
            int online = NetworkStateCache.getOnline(mode.heartbeatId());
            boolean serverOffline = isServerOffline(mode.heartbeatId());
            boolean unavailable = blockJoin || serverOffline;
            List<String> lore = new ArrayList<>();
            if (blockJoin) {
                lore.add("<!italic><" + ModernText.ERROR + "><bold>MANUTENÇÃO</bold>");
                lore.add(ModernText.mini("A rede está em manutenção"));
                lore.add(ModernText.mini("Staff autorizado pode entrar"));
            } else if (serverOffline) {
                lore.add("<!italic><" + ModernText.ERROR + "><bold>INDISPONÍVEL</bold>");
                lore.add(ModernText.mini("Servidor offline ou reiniciando"));
            } else {
                lore.add("<!italic><#94A3B8>Jogadores: <" + ModernText.SUCCESS + ">" + Math.max(online, 0));
                lore.add("");
                lore.add(ModernText.action("▶ Clique para jogar"));
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

    private static ItemStack navIcon(NavEntry entry) {
        ItemStack stack = labeledMini(entry.material(), entry.displayName(), entry.lore());
        ItemMeta meta = stack.getItemMeta();
        if (entry.blocked()) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(LobbyHostPlugin.getInstance(), "nav_blocked"),
                    PersistentDataType.BYTE, (byte) 1);
        } else {
            meta.getPersistentDataContainer().set(MENU_SERVER_KEY, PersistentDataType.STRING, entry.server());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack labeledMini(Material material, String miniName, List<String> miniLore) {
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
