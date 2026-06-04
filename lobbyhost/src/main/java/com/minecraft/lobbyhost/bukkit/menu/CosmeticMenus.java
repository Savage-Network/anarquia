package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.CosmeticTitle;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Gadget;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.ParticleEffect;
import com.minecraft.lobbyhost.bukkit.manager.CosmeticManager;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class CosmeticMenus {

    private static final NamespacedKey COSMETIC_KEY = new NamespacedKey(LobbyHostPlugin.getInstance(), "cosmetic_id");

    private CosmeticMenus() {}

    public static void openMainMenu(Player player) {
        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_main");
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Menu de Cosméticos").color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);

        // Fill background with glass
        fillGlass(inv);

        inv.setItem(10, labeled(Material.ELYTRA, "§a§lCapas Customizadas",
                List.of("§7Mostre sua capa nas costas!", "", "§eClique para abrir.")));
        
        inv.setItem(12, labeled(Material.BLAZE_POWDER, "§6§lPartículas de Aura",
                List.of("§7Efeitos visuais ao seu redor!", "", "§eClique para abrir.")));
        
        inv.setItem(14, labeled(Material.NAME_TAG, "§b§lTítulos Exclusivos",
                List.of("§7Adicione um título acima de seu nome!", "", "§eClique para abrir.")));
        
        inv.setItem(16, labeled(Material.REDSTONE, "§d§lGadgets Divertidos",
                List.of("§7Itens interativos na sua hotbar!", "", "§eClique para abrir.")));

        inv.setItem(22, labeled(Material.BARRIER, "§cDesativar Todos",
                List.of("§7Remove todos os seus cosméticos ativos.")));

        player.openInventory(inv);
    }

    public static void openCapeMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_capes");
        Inventory inv = Bukkit.createInventory(holder, 45,
                Component.text("Capas Customizadas").color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28};
        Cape[] capes = Cape.values();

        for (int i = 0; i < capes.length; i++) {
            Cape cape = capes[i];
            boolean has = account.hasCape(cape);
            boolean active = account.getActiveCape() == cape;

            List<String> lore = new ArrayList<>();
            lore.add("§7Capa: " + cape.getDisplayName());
            lore.add("§7Raridade: " + cape.getRequiredRank().getDisplayName());
            lore.add("");
            if (active) {
                lore.add("§a✔ Equipado");
            } else if (has) {
                lore.add("§e▶ Clique para equipar");
            } else {
                lore.add("§c❌ Bloqueado");
                lore.add("§8Requer rank " + cape.getRequiredRank().getDisplayName());
            }

            ItemStack item = labeled(cape.getIcon(), cape.getDisplayName(), lore, active);
            setCosmeticId(item, cape.getId());
            inv.setItem(slots[i], item);
        }

        inv.setItem(40, labeled(Material.ARROW, "§aVoltar", List.of("§7Clique para voltar ao menu principal")));
        player.openInventory(inv);
    }

    public static void openParticleMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_particles");
        Inventory inv = Bukkit.createInventory(holder, 36,
                Component.text("Partículas de Aura").color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        ParticleEffect[] particles = ParticleEffect.values();

        for (int i = 0; i < particles.length; i++) {
            ParticleEffect effect = particles[i];
            boolean has = account.hasParticle(effect);
            boolean active = account.getActiveParticle() == effect;

            List<String> lore = new ArrayList<>();
            lore.add("§7Partícula: " + effect.getDisplayName());
            lore.add("§7Raridade: " + effect.getRequiredRank().getDisplayName());
            lore.add("");
            if (active) {
                lore.add("§a✔ Equipado");
            } else if (has) {
                lore.add("§e▶ Clique para equipar");
            } else {
                lore.add("§c❌ Bloqueado");
                lore.add("§8Requer rank " + effect.getRequiredRank().getDisplayName());
            }

            ItemStack item = labeled(effect.getIcon(), effect.getDisplayName(), lore, active);
            setCosmeticId(item, effect.getId());
            inv.setItem(slots[i], item);
        }

        inv.setItem(31, labeled(Material.ARROW, "§aVoltar", List.of("§7Clique para voltar ao menu principal")));
        player.openInventory(inv);
    }

    public static void openTitleMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_titles");
        Inventory inv = Bukkit.createInventory(holder, 36,
                Component.text("Títulos Exclusivos").color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        CosmeticTitle[] titles = CosmeticTitle.values();

        for (int i = 0; i < titles.length; i++) {
            CosmeticTitle title = titles[i];
            boolean has = account.hasCosmeticTitle(title);
            boolean active = account.getActiveCosmeticTitle() == title;

            List<String> lore = new ArrayList<>();
            lore.add("§7Título: " + title.getDisplayName());
            lore.add("§7Raridade: " + title.getRequiredRank().getDisplayName());
            lore.add("");
            if (active) {
                lore.add("§a✔ Equipado");
            } else if (has) {
                lore.add("§e▶ Clique para equipar");
            } else {
                lore.add("§c❌ Bloqueado");
                lore.add("§8Requer rank " + title.getRequiredRank().getDisplayName());
            }

            ItemStack item = labeled(title.getIcon(), title.getDisplayName(), lore, active);
            setCosmeticId(item, title.getId());
            inv.setItem(slots[i], item);
        }

        inv.setItem(31, labeled(Material.ARROW, "§aVoltar", List.of("§7Clique para voltar ao menu principal")));
        player.openInventory(inv);
    }

    public static void openGadgetMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_gadgets");
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Gadgets Divertidos").color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {11, 12, 13, 14, 15};
        Gadget[] gadgets = Gadget.values();

        for (int i = 0; i < gadgets.length; i++) {
            Gadget gadget = gadgets[i];
            boolean has = account.hasGadget(gadget);
            boolean active = account.getActiveGadget() == gadget;

            List<String> lore = new ArrayList<>();
            lore.add("§7Gadget: " + gadget.getDisplayName());
            lore.add("§7Raridade: " + gadget.getRequiredRank().getDisplayName());
            lore.add("§7Recarga: §f" + (gadget.getCooldownMs() / 1000.0) + "s");
            lore.add("");
            if (active) {
                lore.add("§a✔ Equipado (Slot 3 da Hotbar)");
            } else if (has) {
                lore.add("§e▶ Clique para equipar");
            } else {
                lore.add("§c❌ Bloqueado");
                lore.add("§8Requer rank " + gadget.getRequiredRank().getDisplayName());
            }

            ItemStack item = labeled(gadget.getMaterial(), gadget.getDisplayName(), lore, active);
            setCosmeticId(item, gadget.getId());
            inv.setItem(slots[i], item);
        }

        inv.setItem(22, labeled(Material.ARROW, "§aVoltar", List.of("§7Clique para voltar ao menu principal")));
        player.openInventory(inv);
    }

    public static void handleClick(Player player, LobbyMenuHolder holder, ItemStack item, int slot) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        Material type = item.getType();
        if (type == Material.GRAY_STAINED_GLASS_PANE) return;

        if (type == Material.ARROW) {
            openMainMenu(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        String menuId = holder.getMenuId();
        if ("cosmetics_main".equals(menuId)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            switch (slot) {
                case 10:
                    openCapeMenu(player);
                    break;
                case 12:
                    openParticleMenu(player);
                    break;
                case 14:
                    openTitleMenu(player);
                    break;
                case 16:
                    openGadgetMenu(player);
                    break;
                case 22:
                    account.setActiveCape(null);
                    account.setActiveParticle(null);
                    account.setActiveCosmeticTitle(null);
                    account.setActiveGadget(null);
                    CosmeticManager.getCapeRenderer().remove(player);
                    
                    // Reseta hotbar
                    com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout.applyDefault(player);
                    TagManager.updateTag(account);
                    
                    player.sendMessage("§aTodos os cosméticos foram desativados!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    openMainMenu(player);
                    break;
            }
            return;
        }

        String cosId = getCosmeticId(item);
        if (cosId == null) return;

        if ("cosmetics_capes".equals(menuId)) {
            Cape cape = Cape.fromId(cosId);
            if (cape == null) return;

            if (!account.hasCape(cape)) {
                player.sendMessage("§cVocê não possui essa capa! Adquira VIP ou compre no site.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (account.getActiveCape() == cape) {
                account.setActiveCape(null);
                CosmeticManager.getCapeRenderer().remove(player);
                player.sendMessage("§aCapa desequipada!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f);
            } else {
                account.setActiveCape(cape);
                CosmeticManager.getCapeRenderer().spawn(player, cape);
                player.sendMessage("§aCapa " + cape.getDisplayName() + " §aequipada!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
            }
            openCapeMenu(player);
            return;
        }

        if ("cosmetics_particles".equals(menuId)) {
            ParticleEffect effect = ParticleEffect.fromId(cosId);
            if (effect == null) return;

            if (!account.hasParticle(effect)) {
                player.sendMessage("§cVocê não possui essa partícula! Adquira VIP ou compre no site.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (account.getActiveParticle() == effect) {
                account.setActiveParticle(null);
                player.sendMessage("§aPartícula desequipada!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f);
            } else {
                account.setActiveParticle(effect);
                player.sendMessage("§aPartícula " + effect.getDisplayName() + " §aequipada!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
            }
            openParticleMenu(player);
            return;
        }

        if ("cosmetics_titles".equals(menuId)) {
            CosmeticTitle title = CosmeticTitle.fromId(cosId);
            if (title == null) return;

            if (!account.hasCosmeticTitle(title)) {
                player.sendMessage("§cVocê não possui esse título! Adquira VIP ou compre no site.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (account.getActiveCosmeticTitle() == title) {
                account.setActiveCosmeticTitle(null);
                player.sendMessage("§aTítulo desequipado!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f);
            } else {
                account.setActiveCosmeticTitle(title);
                player.sendMessage("§aTítulo " + title.getDisplayName() + " §aequipado!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
            }
            TagManager.updateTag(account);
            openTitleMenu(player);
            return;
        }

        if ("cosmetics_gadgets".equals(menuId)) {
            Gadget gadget = Gadget.fromId(cosId);
            if (gadget == null) return;

            if (!account.hasGadget(gadget)) {
                player.sendMessage("§cVocê não possui esse gadget! Adquira VIP ou compre no site.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            if (account.getActiveGadget() == gadget) {
                account.setActiveGadget(null);
                player.sendMessage("§aGadget desequipado!");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 2.0f);
                
                // Reseta a hotbar para remover o gadget
                com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout.applyDefault(player);
            } else {
                account.setActiveGadget(gadget);
                player.sendMessage("§aGadget " + gadget.getDisplayName() + " §aequipado no slot 3!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
                
                // Atualiza hotbar para incluir o gadget
                com.minecraft.lobbyhost.bukkit.lobby.HotbarLayout.applyDefault(player);
            }
            openGadgetMenu(player);
            return;
        }
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }
    }

    private static ItemStack labeled(Material material, String name, List<String> loreLines) {
        return labeled(material, name, loreLines, false);
    }

    private static ItemStack labeled(Material material, String name, List<String> loreLines, boolean glow) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        if (loreLines != null && !loreLines.isEmpty()) {
            meta.lore(loreLines.stream()
                    .map(l -> Component.text(l).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        if (glow) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static void setCosmeticId(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(COSMETIC_KEY, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    private static String getCosmeticId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(COSMETIC_KEY, PersistentDataType.STRING);
    }
}
