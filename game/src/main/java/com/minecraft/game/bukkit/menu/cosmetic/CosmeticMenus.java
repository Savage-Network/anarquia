package com.minecraft.game.bukkit.menu.cosmetic;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.cosmetic.Cape;
import com.minecraft.game.account.context.objects.cosmetic.EmberPlusColor;
import com.minecraft.game.account.context.objects.cosmetic.ParticleEffect;
import com.minecraft.game.account.context.objects.medal.Medal;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.context.objects.toggle.TagPrefix;
import com.minecraft.game.bukkit.cosmetic.CapeParticleRegistry;
import com.minecraft.game.bukkit.cosmetic.CosmeticParticleRegistry;
import com.minecraft.game.bukkit.cosmetic.EmberPlusService;
import com.minecraft.game.bukkit.event.AccountMedalEvent;
import com.minecraft.game.bukkit.event.AccountPrefixTypeEvent;
import com.minecraft.game.bukkit.event.AccountTagEvent;
import com.minecraft.game.bukkit.manager.TagManager;
import com.minecraft.game.message.MenuTexts;
import com.minecraft.game.util.AccountSync;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CosmeticMenus {

    private static final NamespacedKey COSMETIC_KEY = new NamespacedKey(GamePlugin.getInstance(), "cosmetic_id");
    private static final NamespacedKey ACTION_KEY = new NamespacedKey(GamePlugin.getInstance(), "cosmetic_action");

    private CosmeticMenus() {}

    public static void openMainMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        CosmeticMenuHolder holder = new CosmeticMenuHolder("cosmetics_hub");
        Inventory inv = Bukkit.createInventory(holder, 45,
                MenuTexts.component(player, "menus.cosmetic.hub-title"));
        holder.bind(inv);
        fillGlass(inv);

        inv.setItem(4, labeled(player, Material.NETHER_STAR, MenuTexts.legacy(player, "menus.cosmetic.hub-title"),
                List.of(
                        MenuTexts.legacy(player, "menus.cosmetic.hub-desc"),
                        MenuTexts.legacy(player, "menus.cosmetic.hub-desc-2"))));

        inv.setItem(20, hubItem(player, Material.FEATHER, MenuTexts.legacy(player, "menus.cosmetic.capes"),
                MenuTexts.legacy(player, "menus.cosmetic.capes-lore"), "capes"));
        inv.setItem(22, hubItem(player, Material.BLAZE_POWDER, MenuTexts.legacy(player, "menus.cosmetic.particles"),
                MenuTexts.legacy(player, "menus.cosmetic.particles-lore"), "particles"));

        if (account.hasRank(RankType.EMBER_PLUS)) {
            int months = EmberPlusService.effectiveMonths(account);
            inv.setItem(24, hubItem(player, Material.AMETHYST_SHARD, MenuTexts.legacy(player, "menus.cosmetic.ember-color"),
                    MenuTexts.legacy(player, "menus.cosmetic.ember-color-lore"), "ember",
                    MenuTexts.format(player, "menus.cosmetic.ember-months",
                            MenuTexts.map("months", String.valueOf(months)))));
        }

        inv.setItem(40, labeled(player, Material.BARRIER, MenuTexts.legacy(player, "menus.cosmetic.disable-all"),
                List.of(
                        MenuTexts.legacy(player, "menus.cosmetic.disable-all-lore-1"),
                        "",
                        MenuTexts.legacy(player, "menus.cosmetic.disable-all-lore-2"))));

        player.openInventory(inv);
    }

    public static void openCapeMenu(Player player) {
        openCatalog(player, "cosmetics_capes", MenuTexts.legacy(player, "menus.cosmetic.capes-menu"), NamedTextColor.LIGHT_PURPLE, 45,
                Cape.values(), Cape::getId, Cape::getDisplayName, Cape::getIcon, Cape::getRequiredRank,
                account -> account.getActiveCape() != null ? account.getActiveCape().getId() : null,
                account -> account::hasCape);
    }

    public static void openParticleMenu(Player player) {
        openCatalog(player, "cosmetics_particles", MenuTexts.legacy(player, "menus.cosmetic.particles-menu"), NamedTextColor.GOLD, 45,
                ParticleEffect.values(), ParticleEffect::getId, ParticleEffect::getDisplayName,
                ParticleEffect::getIcon, ParticleEffect::getRequiredRank,
                account -> account.getActiveParticle() != null ? account.getActiveParticle().getId() : null,
                account -> account::hasParticle);
    }

    public static void openEmberColorMenu(Player player) {
        openEmberColorMenu(player, "cosmetics_hub");
    }

    public static void openEmberPlusHub(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        if (!account.hasRank(RankType.EMBER_PLUS)) {
            player.sendMessage("§cRequer rank EMBER+.");
            return;
        }

        int months = EmberPlusService.effectiveMonths(account);
        EmberPlusColor color = EmberPlusService.resolveColor(account);
        String preview = EmberPlusService.formatEmberPlusPrefix(account, Tag.EMBER_PLUS);
        if (preview == null) preview = "§5§lEMBER§d+";

        CosmeticMenuHolder holder = new CosmeticMenuHolder("emberplus_hub");
        Inventory inv = Bukkit.createInventory(holder, 54,
                Component.text("EMBER+ | Configuracao").color(NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        inv.setItem(4, labeled(Material.NETHER_STAR, "§5§lEMBER+ Configuracao",
                List.of(
                        "§7Personalize tudo do seu rank",
                        "",
                        "§7Preview: " + preview,
                        "§7Meses: §f" + months,
                        "§7Cor do +: " + color.getDisplayName()
                ), true));

        inv.setItem(20, hubItem(Material.AMETHYST_SHARD, "§5§lCor do +",
                "§7Cor animada do simbolo +", "emberplus_color",
                "§7Atual: " + color.getDisplayName()));
        inv.setItem(21, hubItem(Material.FEATHER, "§d§lAsas EMBER+",
                "§7Capas exclusivas do rank", "emberplus_capes",
                activeLabel(account.getActiveCape() != null ? account.getActiveCape().getDisplayName() : "§8nenhuma")));
        inv.setItem(22, hubItem(Material.BLAZE_POWDER, "§6§lParticulas EMBER+",
                "§7Auras exclusivas do rank", "emberplus_particles",
                activeLabel(account.getActiveParticle() != null ? account.getActiveParticle().getDisplayName() : "§8nenhuma")));
        inv.setItem(23, hubItem(Material.PAPER, "§5§lTag EMBER+",
                "§7Tag exibida no chat/tab", "emberplus_tag",
                account.isUsingTag(Tag.EMBER_PLUS) ? "§a✔ Ativa" : "§7Clique para equipar"));
        inv.setItem(24, hubItem(Material.GOLD_INGOT, "§6§lMedalhas",
                "§7Medalhas do seu rank", "emberplus_medals",
                activeLabel(account.getMedal() != null && account.getMedal() != Medal.NONE
                        ? account.getMedal().getColoredName() : "§8nenhuma")));
        inv.setItem(25, hubItem(Material.OAK_SIGN, "§f§lEstilo de tag",
                "§7Como voce ve prefixos", "emberplus_prefix",
                "§7Modo: §f" + account.getTagPrefix().getDisplayName()));

        inv.setItem(40, labeled(Material.BARRIER, "§cDesativar cosmeticos",
                List.of("§7Remove capa e aura", "", "§eClique para limpar")));
        ItemStack clearItem = inv.getItem(40);
        if (clearItem != null) {
            setAction(clearItem, "emberplus_clear");
            inv.setItem(40, clearItem);
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
    }

    private static String activeLabel(String value) {
        return "§7Ativo: " + value;
    }

    public static void openEmberColorMenu(Player player, String backTarget) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null || !account.hasRank(RankType.EMBER_PLUS)) {
            player.sendMessage("§cRequer rank EMBER+.");
            return;
        }

        int months = EmberPlusService.effectiveMonths(account);
        EmberPlusColor selected = EmberPlusService.resolveColor(account);

        CosmeticMenuHolder holder = new CosmeticMenuHolder("cosmetics_ember", backTarget);
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
        addBack(inv, 49, backTarget, player);
        player.openInventory(inv);
    }

    private static void openEmberCapeMenu(Player player) {
        openEmberCatalog(player, "emberplus_capes", "Asas EMBER+", NamedTextColor.LIGHT_PURPLE,
                Cape.values(), Cape::getId, Cape::getDisplayName, Cape::getIcon, Cape::getRequiredRank,
                account -> account.getActiveCape() != null ? account.getActiveCape().getId() : null,
                account -> account::hasCape);
    }

    private static void openEmberParticleMenu(Player player) {
        openEmberCatalog(player, "emberplus_particles", "Particulas EMBER+", NamedTextColor.GOLD,
                ParticleEffect.values(), ParticleEffect::getId, ParticleEffect::getDisplayName,
                ParticleEffect::getIcon, ParticleEffect::getRequiredRank,
                account -> account.getActiveParticle() != null ? account.getActiveParticle().getId() : null,
                account -> account::hasParticle);
    }

    private static void openEmberTagMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        CosmeticMenuHolder holder = new CosmeticMenuHolder("emberplus_tag", "emberplus_hub");
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Tag EMBER+").color(NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        boolean active = account.isUsingTag(Tag.EMBER_PLUS);
        List<String> lore = new ArrayList<>();
        lore.add("§7Tag: " + Tag.EMBER_PLUS.getColoredName());
        lore.add("");
        if (active) lore.add("§a✔ Equipada");
        else if (account.hasTag(Tag.EMBER_PLUS)) lore.add("§e▶ Clique para equipar");
        else lore.add("§c❌ Indisponivel");

        ItemStack item = labeled(Material.PAPER, Tag.EMBER_PLUS.getColoredName(), lore, active);
        setCosmeticId(item, Tag.EMBER_PLUS.name());
        inv.setItem(13, item);
        addBack(inv, 22, "emberplus_hub", player);
        player.openInventory(inv);
    }

    private static void openEmberMedalMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        CosmeticMenuHolder holder = new CosmeticMenuHolder("emberplus_medals", "emberplus_hub");
        Inventory inv = Bukkit.createInventory(holder, 45,
                Component.text("Medalhas EMBER+").color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {20, 21, 22, 23, 24};
        List<Medal> medals = account.getAvailableMedals();
        int i = 0;
        for (Medal medal : medals) {
            if (i >= slots.length) break;
            boolean active = account.isUsingMedal(medal);
            List<String> lore = new ArrayList<>();
            lore.add("§7Medalha: " + medal.getColoredName());
            lore.add("");
            if (active) lore.add("§a✔ Equipada");
            else lore.add("§e▶ Clique para equipar");
            ItemStack item = labeled(Material.GOLD_NUGGET, medal.getColoredName(), lore, active);
            setCosmeticId(item, medal.name());
            inv.setItem(slots[i++], item);
        }

        ItemStack none = labeled(Material.BARRIER, "§7Sem medalha",
                List.of("§7Remove a medalha ativa", "", "§eClique para limpar"),
                account.getMedal() == Medal.NONE);
        setCosmeticId(none, Medal.NONE.name());
        inv.setItem(31, none);

        addBack(inv, 40, "emberplus_hub", player);
        player.openInventory(inv);
    }

    private static void openEmberPrefixMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        CosmeticMenuHolder holder = new CosmeticMenuHolder("emberplus_prefix", "emberplus_hub");
        Inventory inv = Bukkit.createInventory(holder, 45,
                Component.text("Estilo de tag").color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        int[] slots = {20, 21, 22, 23, 24, 29, 30};
        TagPrefix[] prefixes = TagPrefix.values();
        for (int i = 0; i < prefixes.length && i < slots.length; i++) {
            TagPrefix prefix = prefixes[i];
            boolean active = account.isUsingTagPrefix(prefix);
            List<String> lore = new ArrayList<>();
            lore.add("§7Modo: §f" + prefix.getDisplayName());
            lore.add("");
            if (active) lore.add("§a✔ Selecionado");
            else lore.add("§e▶ Clique para usar");
            ItemStack item = labeled(Material.OAK_SIGN, "§f" + prefix.getDisplayName(), lore, active);
            setCosmeticId(item, prefix.name());
            inv.setItem(slots[i], item);
        }
        addBack(inv, 40, "emberplus_hub", player);
        player.openInventory(inv);
    }

    public static void handleClick(Player player, CosmeticMenuHolder holder, ItemStack item, int slot) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String menuId = holder.getMenuId();

        if ("cosmetics_hub".equals(menuId)) {
            handleHubClick(player, account, item);
            return;
        }

        if ("emberplus_hub".equals(menuId)) {
            handleEmberPlusHubClick(player, account, item);
            return;
        }

        String action = getAction(item);
        if ("back".equals(action)) {
            navigateBack(player, holder.getBackTarget());
            return;
        }

        String cosId = getCosmeticId(item);
        if (cosId == null) return;

        switch (menuId) {
            case "cosmetics_capes" -> handleCape(player, account, cosId, () -> openCapeMenu(player));
            case "cosmetics_particles" -> handleParticle(player, account, cosId, () -> openParticleMenu(player));
            case "cosmetics_ember" -> handleEmberColor(player, account, cosId, holder.getBackTarget());
            case "emberplus_capes" -> handleCape(player, account, cosId, () -> openEmberCapeMenu(player));
            case "emberplus_particles" -> handleParticle(player, account, cosId, () -> openEmberParticleMenu(player));
            case "emberplus_tag" -> handleEmberTag(player, account, cosId);
            case "emberplus_medals" -> handleEmberMedal(player, account, cosId);
            case "emberplus_prefix" -> handleEmberPrefix(player, account, cosId);
        }
    }

    private static void handleHubClick(Player player, Account account, ItemStack item) {
        String action = getAction(item);
        if ("capes".equals(action)) {
            openCapeMenu(player);
        } else if ("particles".equals(action)) {
            openParticleMenu(player);
        } else if ("ember".equals(action)) {
            openEmberColorMenu(player, "cosmetics_hub");
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

    private static void handleCape(Player player, Account account, String cosId, Runnable reopen) {
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
        reopen.run();
    }

    private static void handleParticle(Player player, Account account, String cosId, Runnable reopen) {
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
        reopen.run();
    }

    private static void handleEmberColor(Player player, Account account, String cosId, String backTarget) {
        EmberPlusColor color = EmberPlusColor.fromId(cosId);
        if (color == null) return;
        if (!color.isUnlocked(EmberPlusService.effectiveMonths(account))) {
            deny(player);
            return;
        }
        account.setEmberPlusColor(color.getId());
        refreshTags(player, account);
        player.sendMessage("§aCor EMBER+ alterada para " + color.getDisplayName() + "§a!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.7f);
        openEmberColorMenu(player, backTarget);
    }

    private static void handleEmberPlusHubClick(Player player, Account account, ItemStack item) {
        String action = getAction(item);
        if (action == null) return;
        switch (action) {
            case "emberplus_color" -> openEmberColorMenu(player, "emberplus_hub");
            case "emberplus_capes" -> openEmberCapeMenu(player);
            case "emberplus_particles" -> openEmberParticleMenu(player);
            case "emberplus_tag" -> openEmberTagMenu(player);
            case "emberplus_medals" -> openEmberMedalMenu(player);
            case "emberplus_prefix" -> openEmberPrefixMenu(player);
            case "emberplus_clear" -> {
                clearAll(player, account);
                openEmberPlusHub(player);
            }
        }
    }

    private static void handleEmberTag(Player player, Account account, String raw) {
        Tag tag = Tag.of(raw);
        if (tag != Tag.EMBER_PLUS) return;
        if (!account.hasTag(tag)) {
            deny(player);
            return;
        }
        if (account.isUsingTag(tag)) {
            player.sendMessage("§eVoce ja usa " + tag.getColoredName() + "§e.");
            openEmberTagMenu(player);
            return;
        }
        if (!account.isStaffer() && account.hasCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY)) {
            long left = account.getCooldownRemaining(Constant.TAG_CHANGE_COOLDOWN_KEY);
            player.sendMessage("§cAguarde " + Math.max(1, left / 1000L) + "s para trocar de tag novamente.");
            return;
        }
        account.setTag(tag);
        if (!account.isStaffer()) {
            account.setCooldown(Constant.TAG_CHANGE_COOLDOWN_KEY, Constant.TAG_CHANGE_COOLDOWN_MS);
        }
        org.bukkit.Bukkit.getPluginManager().callEvent(new AccountTagEvent(account, tag));
        AccountSync.saveAndBroadcast(account);
        refreshTags(player, account);
        player.sendMessage("§aTag alterada para " + tag.getColoredName() + "§a.");
        openEmberTagMenu(player);
    }

    private static void handleEmberMedal(Player player, Account account, String raw) {
        Medal medal = Medal.of(raw);
        if (medal == null) return;
        if (medal != Medal.NONE && !account.hasMedal(medal)) {
            deny(player);
            return;
        }
        if (account.isUsingMedal(medal)) {
            player.sendMessage("§eVoce ja usa " + medal.getColoredName() + "§e.");
            openEmberMedalMenu(player);
            return;
        }
        account.setMedal(medal);
        org.bukkit.Bukkit.getPluginManager().callEvent(new AccountMedalEvent(account, medal));
        AccountSync.saveAndBroadcast(account);
        player.sendMessage(medal == Medal.NONE
                ? "§eMedalha removida."
                : "§aMedalha alterada para " + medal.getColoredName() + "§a.");
        openEmberMedalMenu(player);
    }

    private static void handleEmberPrefix(Player player, Account account, String raw) {
        TagPrefix prefix = TagPrefix.of(raw);
        if (prefix == null) return;
        if (account.isUsingTagPrefix(prefix)) {
            player.sendMessage("§eVoce ja usa o modo " + prefix.getDisplayName() + ".");
            openEmberPrefixMenu(player);
            return;
        }
        account.setTagPrefix(prefix);
        org.bukkit.Bukkit.getPluginManager().callEvent(new AccountPrefixTypeEvent(account, prefix));
        AccountSync.saveAndBroadcast(account);
        refreshTags(player, account);
        player.sendMessage("§aModo de prefixo alterado para " + prefix.getDisplayName() + ".");
        openEmberPrefixMenu(player);
    }

    private static void navigateBack(Player player, String backTarget) {
        if ("emberplus_hub".equals(backTarget)) {
            openEmberPlusHub(player);
        } else {
            openMainMenu(player);
        }
    }

    private static void deny(Player player) {
        player.sendMessage(MenuTexts.legacy(player, "menus.cosmetic.deny"));
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

    private static <T> void openEmberCatalog(Player player, String menuId, String title, NamedTextColor color,
                                             T[] values,
                                             Function<T, String> idFn,
                                             Function<T, String> nameFn,
                                             Function<T, Material> iconFn,
                                             Function<T, RankType> rankFn,
                                             ActiveIdResolver activeFn,
                                             HasResolver<T> hasFn) {
        List<T> filtered = new ArrayList<>();
        for (T entry : values) {
            if (rankFn.apply(entry) == RankType.EMBER_PLUS) {
                filtered.add(entry);
            }
        }
        @SuppressWarnings("unchecked")
        T[] emberOnly = filtered.toArray((T[]) java.lang.reflect.Array.newInstance(
                values.getClass().getComponentType(), filtered.size()));
        openCatalog(player, menuId, "emberplus_hub", title, color, 45, emberOnly,
                idFn, nameFn, iconFn, rankFn, activeFn, hasFn);
    }

    private static <T> void openCatalog(Player player, String menuId, String backTarget, String title,
                                          NamedTextColor color, int size,
                                          T[] values,
                                          Function<T, String> idFn,
                                          Function<T, String> nameFn,
                                          Function<T, Material> iconFn,
                                          Function<T, RankType> rankFn,
                                          ActiveIdResolver activeFn,
                                          HasResolver<T> hasFn) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        CosmeticMenuHolder holder = new CosmeticMenuHolder(menuId, backTarget);
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
            List<String> lore = baseLore(player, nameFn.apply(entry), rankFn.apply(entry), owned, active);
            ItemStack item = labeled(player, iconFn.apply(entry), nameFn.apply(entry), lore, active);
            setCosmeticId(item, idFn.apply(entry));
            inv.setItem(slots[i], item);
        }
        addBack(inv, size - 5, backTarget, player);
        player.openInventory(inv);
    }

    private static <T> void openCatalog(Player player, String menuId, String title, NamedTextColor color, int size,
                                        T[] values,
                                        Function<T, String> idFn,
                                        Function<T, String> nameFn,
                                        Function<T, Material> iconFn,
                                        Function<T, RankType> rankFn,
                                        ActiveIdResolver activeFn,
                                        HasResolver<T> hasFn) {
        openCatalog(player, menuId, "cosmetics_hub", title, color, size, values,
                idFn, nameFn, iconFn, rankFn, activeFn, hasFn);
    }

    private static List<String> baseLore(Player player, String name, RankType rank, boolean has, boolean active) {
        List<String> lore = new ArrayList<>();
        lore.add(MenuTexts.format(player, "menus.cosmetic.item-label", MenuTexts.map("name", name)));
        lore.add(MenuTexts.format(player, "menus.cosmetic.rarity", MenuTexts.map("rank", rank.getDisplayName())));
        lore.add("");
        if (active) lore.add(MenuTexts.legacy(player, "menus.common.equipped"));
        else if (has) lore.add(MenuTexts.legacy(player, "menus.cosmetic.click-equip"));
        else {
            lore.add(MenuTexts.legacy(player, "menus.cosmetic.click-blocked"));
            lore.add(MenuTexts.format(player, "menus.cosmetic.requires-rank-line",
                    MenuTexts.map("rank", rank.getDisplayName())));
        }
        return lore;
    }

    private static ItemStack hubItem(Player player, Material material, String name, String line1, String action) {
        return hubItem(player, material, name, line1, action, MenuTexts.legacy(player, "menus.common.click-open"));
    }

    private static ItemStack hubItem(Player player, Material material, String name, String line1, String action, String line2) {
        ItemStack item = labeled(player, material, name, List.of(line1, "", line2));
        setAction(item, action);
        return item;
    }

    private static void addBack(Inventory inv, int slot, Player player) {
        addBack(inv, slot, "cosmetics_hub", player);
    }

    private static void addBack(Inventory inv, int slot, String backTarget, Player player) {
        String label = "emberplus_hub".equals(backTarget)
                ? MenuTexts.legacy(player, "menus.cosmetic.back-ember")
                : MenuTexts.legacy(player, "menus.cosmetic.back-main");
        ItemStack back = labeled(player, Material.ARROW, MenuTexts.legacy(player, "menus.common.back"), List.of(label));
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

    private static ItemStack labeled(Player player, Material material, String name, List<String> loreLines) {
        return labeled(player, material, name, loreLines, false);
    }

    private static ItemStack labeled(Player player, Material material, String name, List<String> loreLines, boolean glow) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        if (loreLines != null && !loreLines.isEmpty()) {
            meta.lore(loreLines.stream()
                    .map(l -> Component.text(l).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        if (glow) {
            meta.setEnchantmentGlintOverride(true);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    /** Compat interna — preferir {@link #labeled(Player, Material, String, List, boolean)}. */
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
            meta.setEnchantmentGlintOverride(true);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack labeled(Material material, String name, List<String> loreLines) {
        return labeled(material, name, loreLines, false);
    }

    private static ItemStack hubItem(Material material, String name, String line1, String action) {
        return hubItem(material, name, line1, action, "§eClique para abrir");
    }

    private static ItemStack hubItem(Material material, String name, String line1, String action, String line2) {
        ItemStack item = labeled(material, name, List.of(line1, "", line2));
        setAction(item, action);
        return item;
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

    static void refreshTags(Player player, Account account) {
        TagManager.updateTag(account);
    }
}
