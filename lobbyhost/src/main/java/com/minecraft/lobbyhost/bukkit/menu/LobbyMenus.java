package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.display.npc.NpcDefinition;
import com.minecraft.lobbyhost.display.npc.NpcManager;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Menus simples da hotbar do hub (navegacao, perfil, cosmeticos, missoes).
 */
public final class LobbyMenus {

    private static final NamespacedKey MENU_NPC_KEY =
            new NamespacedKey(LobbyHostPlugin.getInstance(), "menu_npc");

    private LobbyMenus() {}

    public static void openNavigation(Player player) {
        List<NpcDefinition> modes = new ArrayList<>(NpcManager.listDefinitions());
        int size = Math.min(54, Math.max(9, ((modes.size() + 8) / 9) * 9));
        LobbyMenuHolder holder = new LobbyMenuHolder("navigation");
        Inventory inv = Bukkit.createInventory(holder, size,
                Component.text("Modos de jogo").color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);

        int slot = 0;
        for (NpcDefinition def : modes) {
            if (slot >= size) break;
            inv.setItem(slot++, modeIcon(def));
        }

        inv.setItem(size - 5, labeled(Material.BARRIER, "§cFechar", List.of()));
        player.openInventory(inv);
    }

    public static void openProfile(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
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
        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics");
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Cosmeticos").color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        inv.setItem(13, labeled(Material.NETHER_STAR, "§e§lEm breve",
                List.of("§7Cosmeticos serao adicionados", "§7na proxima fase.")));
        inv.setItem(22, labeled(Material.BARRIER, "§cFechar", List.of()));
        player.openInventory(inv);
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
        String npcId = item.getItemMeta().getPersistentDataContainer()
                .get(MENU_NPC_KEY, PersistentDataType.STRING);
        if (npcId == null) return;
        NpcDefinition def = NpcManager.getDefinition(npcId);
        if (def != null) {
            player.closeInventory();
            NpcManager.handleClick(player, def);
        }
    }

    public static void connectBungee(Player player, String server) {
        LobbyHostPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        player.closeInventory();
        player.sendPluginMessage(plugin, "BungeeCord",
                ("Connect\0" + server).getBytes(StandardCharsets.UTF_8));
        player.sendMessage(Component.text("§aConectando em §f" + server + "§a..."));
    }

    private static ItemStack modeIcon(NpcDefinition def) {
        Material mat = switch (def.getId().toLowerCase()) {
            case "npc_bw" -> Material.RED_BED;
            case "npc_duels" -> Material.IRON_SWORD;
            case "npc_ew" -> Material.DRAGON_EGG;
            case "npc_sw" -> Material.ELYTRA;
            case "npc_tb" -> Material.STONE_BRICKS;
            default -> Material.COMPASS;
        };
        List<String> lore = new ArrayList<>();
        lore.add("§7Clique para entrar");
        if (def.getActionType() == com.minecraft.lobbyhost.display.npc.NpcActionType.BUNGEE
                && def.getActionValue() != null) {
            lore.add("§8Servidor: §f" + def.getActionValue());
        }
        ItemStack stack = labeled(mat, "§f§l" + def.getDisplayName(), lore);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(MENU_NPC_KEY, PersistentDataType.STRING, def.getId());
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
}
