package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.LobbySettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.minecraft.lobbyhost.util.BungeeCordUtil;
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
        List<NavEntry> entries = defaultNavigationEntries();
        LobbyMenuHolder holder = new LobbyMenuHolder("navigation");
        String title = LobbySettings.NAVIGATION_TITLE;
        Inventory inv = Bukkit.createInventory(holder, NAV_SIZE,
                Component.text(title.replace('&', '§'))
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);

        fillGlass(inv, entries);

        for (NavEntry entry : entries) {
            if (entry.slot() >= 0 && entry.slot() < NAV_SIZE && entry.slot() != CLOSE_SLOT) {
                inv.setItem(entry.slot(), navIcon(entry));
            }
        }

        inv.setItem(CLOSE_SLOT, labeled(Material.BARRIER, "§cFechar", List.of()));
        player.openInventory(inv);
    }

    public static void openProfile(Player player) {
        var account = Core.getAccountController().of(player.getUniqueId());
        LobbyMenuHolder holder = new LobbyMenuHolder("profile");
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Seu perfil").color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);

        List<String> lore = new ArrayList<>();
        if (account != null) {
            lore.add("§7Rank: §f" + (account.getRank() != null ? account.getRank().getType().name() : "-"));
            lore.add("§7Tag: §f" + (account.getTag() != null ? account.getTag().name() : "-"));
            lore.add("§7Medalha: §f" + (account.getMedal() != null ? account.getMedal().name() : "-"));
            lore.add("§7Amigos: §f" + (account.getFriendMetadata() != null
                    ? account.getFriendMetadata().getFriends().size() : 0));
        } else {
            lore.add("§cConta nao carregada.");
        }
        lore.add("§8/prefs para preferencias");

        inv.setItem(13, labeled(Material.PLAYER_HEAD, "§b§l" + player.getName(), lore));
        inv.setItem(22, labeled(Material.BARRIER, "§cFechar", List.of()));
        player.openInventory(inv);
    }

    public static void openCosmetics(Player player) {
        CosmeticMenus.openMainMenu(player);
    }

    public static void openMissions(Player player) {
        LobbyMenuHolder holder = new LobbyMenuHolder("missions");
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Missoes").color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        inv.setItem(13, labeled(Material.CHEST, "§6§lEm breve",
                List.of("§7Missoes diarias em desenvolvimento.")));
        inv.setItem(22, labeled(Material.BARRIER, "§cFechar", List.of()));
        player.openInventory(inv);
    }

    public static void handleNavigationClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String server = item.getItemMeta().getPersistentDataContainer()
                .get(MENU_SERVER_KEY, PersistentDataType.STRING);
        if (server == null || server.isBlank()) return;
        connectBungee(player, server);
    }

    public static void connectBungee(Player player, String server) {
        LobbyHostPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        player.closeInventory();
        BungeeCordUtil.connect(player, plugin, server);
        player.sendMessage(Component.text("§aConectando em §f" + server + "§a..."));
    }

    private static List<NavEntry> defaultNavigationEntries() {
        return List.of(
                new NavEntry("semi-anarquia", "semi-anarquia", "§4§lSemi Anarquia",
                        Material.DIAMOND_SWORD, 11, List.of(
                        "§7Sobreviva, PvP, economia",
                        "§7e clãs na rede.",
                        "",
                        "§a▶ Clique para jogar")),
                new NavEntry("eventos", "eventos", "§d§lEventos",
                        Material.FIREWORK_ROCKET, 15, List.of(
                        "§7Torneios e minigames",
                        "§7especiais da rede.",
                        "",
                        "§a▶ Clique para entrar"))
        );
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
        ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack navIcon(NavEntry entry) {
        ItemStack stack = labeled(entry.material(), entry.displayName(), entry.lore());
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(MENU_SERVER_KEY, PersistentDataType.STRING, entry.server());
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack labeled(Material material, String name, List<String> loreLines) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        if (loreLines != null && !loreLines.isEmpty()) {
            meta.lore(loreLines.stream()
                    .map(l -> Component.text(l).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private record NavEntry(String id, String server, String displayName, Material material,
                            int slot, List<String> lore) {}
}
