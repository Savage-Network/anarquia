package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * Hotbar padrao do lobby.
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

        inv.setItem(0, item(Material.COMPASS,
                ModernText.gradient("Jogar", ModernText.SUCCESS, ModernText.ACCENT_A),
                List.of(
                        ModernText.mini("Semi Anarquia e Eventos"),
                        ModernText.action("Clique direito")
                ), PDC_COMPASS));
        inv.setItem(1, item(Material.NETHER_STAR,
                ModernText.brandGradient("Cosméticos"),
                List.of(ModernText.action("Clique direito")), PDC_COSMETIC));

        com.minecraft.lobbyhost.account.Account account =
                com.minecraft.lobbyhost.Core.getAccountController().of(player.getUniqueId());
        if (account != null) {
            var gadget = account.getActiveGadget();
            if (gadget != null) {
                inv.setItem(2, item(gadget.getMaterial(),
                        ModernText.gradient(gadget.getDisplayName(), "#C084FC", "#F472B6"),
                        List.of(
                                ModernText.action("Clique direito para usar"),
                                ModernText.mini("Recarga: " + (gadget.getCooldownMs() / 1000.0) + "s")
                        ), "lobby_gadget"));
            }
        }

        inv.setItem(4, skull(player,
                ModernText.gradient("Perfil", ModernText.ACCENT_A, ModernText.ACCENT_B),
                List.of(ModernText.action("Clique direito")), PDC_PROFILE));
        var vis = PlayerVisibilityService.get(player);
        inv.setItem(7, item(vis.getMaterial(), vis.getDisplayNameMini(),
                List.of(ModernText.mini("Clique para alternar")), PDC_VISIBILITY));
        inv.setItem(8, item(Material.CHEST,
                ModernText.brandGradient("Missões"),
                List.of(ModernText.action("Clique direito")), PDC_MISSION));

        inv.setHeldItemSlot(0);
        player.updateInventory();
    }

    private static ItemStack item(Material material, String miniName, List<String> miniLore, String pdcKey) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.noItalic(ModernText.parse(miniName)));
        meta.lore(miniLore.stream().map(l -> ModernText.noItalic(ModernText.parse(l))).toList());
        meta.getPersistentDataContainer().set(
                HotbarKeys.SLOT_KEY,
                org.bukkit.persistence.PersistentDataType.STRING,
                pdcKey);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack skull(Player owner, String miniName, List<String> miniLore, String pdcKey) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(owner);
        meta.displayName(ModernText.noItalic(ModernText.parse(miniName)));
        meta.lore(miniLore.stream().map(l -> ModernText.noItalic(ModernText.parse(l))).toList());
        meta.getPersistentDataContainer().set(
                HotbarKeys.SLOT_KEY,
                org.bukkit.persistence.PersistentDataType.STRING,
                pdcKey);
        stack.setItemMeta(meta);
        return stack;
    }
}
