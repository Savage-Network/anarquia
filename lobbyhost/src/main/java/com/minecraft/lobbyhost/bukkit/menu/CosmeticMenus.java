package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.EmberPlusColor;
import com.minecraft.lobbyhost.account.context.objects.cosmetic.ParticleEffect;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.bukkit.cosmetic.CapeParticleRegistry;
import com.minecraft.lobbyhost.bukkit.cosmetic.CosmeticParticleRegistry;
import com.minecraft.lobbyhost.bukkit.cosmetic.EmberPlusService;
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
    private static final NamespacedKey ACTION_KEY = new NamespacedKey(LobbyHostPlugin.getInstance(), "cosmetic_action");

    private CosmeticMenus() {}

    public static void openMainMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_hub");
        Inventory inv = Bukkit.createInventory(holder, 45,
                Component.text("Cosméticos").color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        inv.setItem(4, labeled(Material.NETHER_STAR, "§d§lCosméticos",
                List.of("§7Personalize sua aparência", "§7Asas de partículas e auras")));

        inv.setItem(20, hubItem(Material.FEATHER, "§d§lAsas / Capas", "§7Asas de partículas nas costas", "capes"));
        inv.setItem(22, hubItem(Material.BLAZE_POWDER, "§6§lPartículas", "§7Auras visuais ao redor do corpo", "particles"));

        if (account.hasRank(RankType.EMBER_PLUS)) {
            int months = EmberPlusService.effectiveMonths(account);
            inv.setItem(24, hubItem(Material.AMETHYST_SHARD, "§5§lEmber+ Cor",
                    "§7Cor do + do seu rank", "ember",
                    "§7Meses: §f" + months));
        }

        inv.setItem(40, labeled(Material.BARRIER, "§cDesativar tudo",
                List.of("§7Remove capa e aura", "", "§eClique para limpar")));

        player.openInventory(inv);
    }

    public static void openCapeMenu(Player player) {
        openCatalog(player, "cosmetics_capes", "Asas de Partículas", NamedTextColor.LIGHT_PURPLE, 45,
                Cape.values(), Cape::getId, Cape::getDisplayName, Cape::getIcon, Cape::getRequiredRank,
                account -> account.getActiveCape() != null ? account.getActiveCape().getId() : null,
                account -> account::hasCape);
    }

    public static void openParticleMenu(Player player) {
        openCatalog(player, "cosmetics_particles", "Partículas de Aura", NamedTextColor.GOLD, 45,
                ParticleEffect.values(), ParticleEffect::getId, ParticleEffect::getDisplayName,
                ParticleEffect::getIcon, ParticleEffect::getRequiredRank,
                account -> account.getActiveParticle() != null ? account.getActiveParticle().getId() : null,
                account -> account::hasParticle);
    }

    public static void openEmberColorMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null || !account.hasRank(RankType.EMBER_PLUS)) {
            player.sendMessage("§cRequer rank EMBER+.");
            return;
        }

        int months = EmberPlusService.effectiveMonths(account);
        EmberPlusColor selected = EmberPlusService.resolveColor(account);

        LobbyMenuHolder holder = new LobbyMenuHolder("cosmetics_ember");
        Inventory inv = Bukkit.createInventory(holder, 54,
                Component.text("Ember+ | Cores").color(NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        inv.setItem(4, labeled(Material.CLOCK, "§5§lAssinatura EMBER+",
                List.of("§7Meses acumulados: §f" + months,
                        "§71 mês: cores básicas",
                        "§73 meses: +6 cores",
                        "§76 meses: +4 cores",
                        "§712 meses: arco-íris e yin yang")));

        int[] slots = {19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41};
        EmberPlusColor[] colors = EmberPlusColor.values();
        for (int i = 0; i < colors.length && i < slots.length; i++) {
            EmberPlusColor color = colors[i];
            boolean unlocked = color.isUnlocked(months);
            boolean active = color == selected;
            List<String> lore = new ArrayList<>();
            lore.add("§7Cor: " + color.getDisplayName());
            lore.add("§7Requer: §f" + color.getMinMonths() + " mês(es)");
            lore.add("");
            if (active) lore.add("§a✔ Selecionada");
            else if (unlocked) lore.add("§e▶ Clique para selecionar");
            else lore.add("§c❌ Bloqueada");
            ItemStack item = labeled(color.getIcon(), color.getDisplayName(), lore, active);
            setCosmeticId(item, color.getId());
            inv.setItem(slots[i], item);
        }
        addBack(inv, 49);
        player.openInventory(inv);
    }

    public static void handleClick(Player player, LobbyMenuHolder holder, ItemStack item, int slot) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String menuId = holder.getMenuId();

        if ("cosmetics_hub".equals(menuId)) {
            handleHubClick(player, account, item);
            return;
        }

        String action = getAction(item);
        if ("back".equals(action)) {
            openMainMenu(player);
            return;
        }

        String cosId = getCosmeticId(item);
        if (cosId == null) return;

        switch (menuId) {
            case "cosmetics_capes" -> handleCape(player, account, cosId);
            case "cosmetics_particles" -> handleParticle(player, account, cosId);
            case "cosmetics_ember" -> handleEmberColor(player, account, cosId);
        }
    }

    private static void handleHubClick(Player player, Account account, ItemStack item) {
        String action = getAction(item);
        if ("capes".equals(action)) {
            openCapeMenu(player);
        } else if ("particles".equals(action)) {
            openParticleMenu(player);
        } else if ("ember".equals(action)) {
            openEmberColorMenu(player);
        } else if (item.getType() == Material.BARRIER) {
            clearAll(player, account);
            openMainMenu(player);
        }
    }

    private static void clearAll(Player player, Account account) {
        account.setActiveCape(null);
        account.setActiveParticle(null);
        CapeParticleRegistry.clear(player.getUniqueId());
        CosmeticParticleRegistry.clear(player.getUniqueId());
        player.sendMessage("§aTodos os cosméticos foram desativados.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1.5f);
    }

    private static void handleCape(Player player, Account account, String cosId) {
        Cape cape = Cape.fromId(cosId);
        if (cape == null) return;
        if (!account.hasCape(cape)) {
            deny(player);
            return;
        }
        if (account.getActiveCape() == cape) {
            account.setActiveCape(null);
            player.sendMessage("§aAsas desequipadas!");
        } else {
            account.setActiveCape(cape);
            player.sendMessage("§aAsas " + cape.getDisplayName() + " §aequipadas!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
        }
        openCapeMenu(player);
    }

    private static void handleParticle(Player player, Account account, String cosId) {
        ParticleEffect effect = ParticleEffect.fromId(cosId);
        if (effect == null) return;
        if (!account.hasParticle(effect)) {
            deny(player);
            return;
        }
        if (account.getActiveParticle() == effect) {
            account.setActiveParticle(null);
            player.sendMessage("§aAura desativada!");
        } else {
            account.setActiveParticle(effect);
            player.sendMessage("§aAura " + effect.getDisplayName() + " §aativada!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.6f);
        }
        openParticleMenu(player);
    }

    private static void handleEmberColor(Player player, Account account, String cosId) {
        EmberPlusColor color = EmberPlusColor.fromId(cosId);
        if (color == null) return;
        if (!color.isUnlocked(EmberPlusService.effectiveMonths(account))) {
            deny(player);
            return;
        }
        account.setEmberPlusColor(color.getId());
        TagManager.refresh(player);
        player.sendMessage("§aCor EMBER+ alterada para " + color.getDisplayName() + "§a!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.7f);
        openEmberColorMenu(player);
    }

    private static void deny(Player player) {
        player.sendMessage("§cVocê não possui este cosmético!");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    @FunctionalInterface
    private interface AccessCheck<T> {
        boolean check(T value);
    }

    @FunctionalInterface
    private interface ActiveIdResolver {
        String resolve(Account account);
    }

    @FunctionalInterface
    private interface HasResolver<T> {
        AccessCheck<T> resolve(Account account);
    }

    private static <T> void openCatalog(Player player, String menuId, String title, NamedTextColor color, int size,
                                        T[] values,
                                        java.util.function.Function<T, String> idFn,
                                        java.util.function.Function<T, String> nameFn,
                                        java.util.function.Function<T, Material> iconFn,
                                        java.util.function.Function<T, RankType> rankFn,
                                        ActiveIdResolver activeFn,
                                        HasResolver<T> hasFn) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        LobbyMenuHolder holder = new LobbyMenuHolder(menuId);
        Inventory inv = Bukkit.createInventory(holder, size,
                Component.text(title).color(color).decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28};
        String activeId = activeFn.resolve(account);
        AccessCheck<T> has = hasFn.resolve(account);

        for (int i = 0; i < values.length && i < slots.length; i++) {
            T entry = values[i];
            boolean owned = has.check(entry);
            boolean active = idFn.apply(entry).equalsIgnoreCase(activeId);
            List<String> lore = baseLore(nameFn.apply(entry), rankFn.apply(entry), owned, active);
            ItemStack item = labeled(iconFn.apply(entry), nameFn.apply(entry), lore, active);
            setCosmeticId(item, idFn.apply(entry));
            inv.setItem(slots[i], item);
        }
        addBack(inv, size - 5);
        player.openInventory(inv);
    }

    private static List<String> baseLore(String name, RankType rank, boolean has, boolean active) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Item: " + name);
        lore.add("§7Raridade: " + rank.getDisplayName());
        lore.add("");
        if (active) lore.add("§a✔ Equipado");
        else if (has) lore.add("§e▶ Clique para equipar");
        else {
            lore.add("§c❌ Bloqueado");
            lore.add("§8Requer rank " + rank.getDisplayName());
        }
        return lore;
    }

    private static ItemStack hubItem(Material material, String name, String line1, String action) {
        return hubItem(material, name, line1, action, "§eClique para abrir");
    }

    private static ItemStack hubItem(Material material, String name, String line1, String action, String line2) {
        ItemStack item = labeled(material, name, List.of(line1, "", line2));
        setAction(item, action);
        return item;
    }

    private static void addBack(Inventory inv, int slot) {
        ItemStack back = labeled(Material.ARROW, "§aVoltar", List.of("§7Menu principal"));
        setAction(back, "back");
        inv.setItem(slot, back);
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

    private static void setAction(ItemStack item, String action) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
    }

    private static String getAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
    }
}
