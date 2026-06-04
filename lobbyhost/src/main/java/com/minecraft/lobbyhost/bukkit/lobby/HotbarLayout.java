package com.minecraft.lobbyhost.bukkit.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * Hotbar padrao do lobby - espelho de {@code Architect.handleHotbar} do Savage.
 *
 * <p>Slot 0 navegacao, 1 cosmeticos, 4 perfil, 7 visibilidade, 8 missoes.
 * Itens sao identificados via PDC nos listeners para abrir o menu correto.
 */
public final class HotbarLayout {

    public static final String PDC_COMPASS = "lobby_compass";
    public static final String PDC_COSMETIC = "lobby_cosmetic";
    public static final String PDC_PROFILE = "lobby_profile";
    public static final String PDC_VISIBILITY = "lobby_visibility";
    public static final String PDC_MISSION = "lobby_mission";

    public static void applyDefault(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setItem(0, item(Material.COMPASS, "§a§lServidores",
                List.of("§7Semi Anarquia e Eventos", "§eClique direito"), PDC_COMPASS));
        inv.setItem(1, item(Material.NETHER_STAR, "§e§lCosmeticos", List.of("§7Clique direito"), PDC_COSMETIC));
        
        // Gadget ativo (Slot 2)
        com.minecraft.lobbyhost.account.Account account = com.minecraft.lobbyhost.Core.getAccountController().of(player.getUniqueId());
        if (account != null) {
            com.minecraft.lobbyhost.account.context.objects.cosmetic.Gadget gadget = account.getActiveGadget();
            if (gadget != null) {
                inv.setItem(2, item(gadget.getMaterial(), "§d§l" + gadget.getDisplayName(), 
                        List.of("§7Clique direito para usar!", "§7Recarga: §f" + (gadget.getCooldownMs() / 1000.0) + "s"), "lobby_gadget"));
            }
        }

        inv.setItem(4, skull(player, "§b§lPerfil", List.of("§7Clique direito"), PDC_PROFILE));
        var vis = PlayerVisibilityService.get(player);
        inv.setItem(7, item(vis.getMaterial(), vis.getDisplayName(),
                List.of("§7Clique para alternar"), PDC_VISIBILITY));
        inv.setItem(8, item(Material.CHEST, "§6§lMissoes", List.of("§7Clique direito"), PDC_MISSION));

        inv.setHeldItemSlot(0);
        player.updateInventory();
    }

    private static ItemStack item(Material material, String name, List<String> lore, String pdcKey) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore.stream().map(l -> (Component) Component.text(l)
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)).toList());
        meta.getPersistentDataContainer().set(
                HotbarKeys.SLOT_KEY,
                org.bukkit.persistence.PersistentDataType.STRING,
                pdcKey);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack skull(Player owner, String name, List<String> lore, String pdcKey) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(owner);
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore.stream().map(l -> (Component) Component.text(l)
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)).toList());
        meta.getPersistentDataContainer().set(
                HotbarKeys.SLOT_KEY,
                org.bukkit.persistence.PersistentDataType.STRING,
                pdcKey);
        stack.setItemMeta(meta);
        return stack;
    }
}
