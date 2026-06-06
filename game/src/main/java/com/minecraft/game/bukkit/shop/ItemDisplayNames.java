package com.minecraft.game.bukkit.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;
import java.util.Map;

/**
 * Nomes amigáveis de itens para lojas e leilão (pt-BR + cores por categoria).
 */
public final class ItemDisplayNames {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private static final Map<String, String> WORDS_PT = Map.ofEntries(
            Map.entry("iron", "Ferro"),
            Map.entry("golden", "Dourado"),
            Map.entry("gold", "Ouro"),
            Map.entry("diamond", "Diamante"),
            Map.entry("netherite", "Netherita"),
            Map.entry("emerald", "Esmeralda"),
            Map.entry("stone", "Pedra"),
            Map.entry("cobblestone", "Pedregulho"),
            Map.entry("oak", "Carvalho"),
            Map.entry("spruce", "Abeto"),
            Map.entry("birch", "Bétula"),
            Map.entry("jungle", "Selva"),
            Map.entry("acacia", "Acácia"),
            Map.entry("dark", "Escuro"),
            Map.entry("cherry", "Cerejeira"),
            Map.entry("mangrove", "Mangue"),
            Map.entry("bamboo", "Bambu"),
            Map.entry("crimson", "Carmesim"),
            Map.entry("warped", "Distorcido"),
            Map.entry("sword", "Espada"),
            Map.entry("pickaxe", "Picareta"),
            Map.entry("axe", "Machado"),
            Map.entry("shovel", "Pá"),
            Map.entry("hoe", "Enxada"),
            Map.entry("helmet", "Capacete"),
            Map.entry("chestplate", "Peitoral"),
            Map.entry("leggings", "Calças"),
            Map.entry("boots", "Botas"),
            Map.entry("bow", "Arco"),
            Map.entry("arrow", "Flecha"),
            Map.entry("shield", "Escudo"),
            Map.entry("apple", "Maçã"),
            Map.entry("beef", "Carne"),
            Map.entry("cooked", "Cozido"),
            Map.entry("bread", "Pão"),
            Map.entry("pearl", "Pérola"),
            Map.entry("ender", "Ender"),
            Map.entry("chest", "Baú"),
            Map.entry("torch", "Tocha"),
            Map.entry("glass", "Vidro"),
            Map.entry("obsidian", "Obsidiana"),
            Map.entry("anvil", "Bigorna"),
            Map.entry("furnace", "Fornalha"),
            Map.entry("table", "Mesa"),
            Map.entry("crafting", "Trabalho"),
            Map.entry("experience", "Experiência"),
            Map.entry("bottle", "Frasco"),
            Map.entry("shears", "Tesoura"),
            Map.entry("log", "Tronco"),
            Map.entry("planks", "Tábuas"),
            Map.entry("ingot", "Barra"),
            Map.entry("nugget", "Pepita"),
            Map.entry("block", "Bloco")
    );

    private ItemDisplayNames() {}

    /** Linha colorida para placa de loja. */
    public static String signLine(Material material) {
        String color = accent(material);
        String bold = isValuable(material) ? "§l" : "";
        return color + bold + plain(material);
    }

    /** Resolve material a partir do texto da placa (enum ou nome traduzido). */
    public static Material fromDisplayName(String input) {
        if (input == null || input.isBlank()) return null;
        String stripped = stripColors(input).trim();
        Material direct = Material.matchMaterial(stripped.toUpperCase(java.util.Locale.ROOT));
        if (direct != null && direct.isItem()) return direct;
        for (Material material : Material.values()) {
            if (!material.isItem()) continue;
            if (plain(material).equalsIgnoreCase(stripped)) return material;
        }
        return null;
    }

    /** Nome legível sem cores (chat, logs). */
    public static String plain(Material material) {
        if (material == null || material.isAir()) return "Ar";
        return translate(material);
    }

    /** Nome legível de um stack (respeita displayName custom se existir). */
    public static String plain(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return "Ar";
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return stripColors(meta.getDisplayName());
        }
        return plain(stack.getType());
    }

    /** Título colorido para slot de menu (loja / leilão). */
    public static String menuTitle(Material material) {
        return menuTitle(material, 1);
    }

    public static String menuTitle(Material material, int amount) {
        String color = accent(material);
        String bold = isValuable(material) ? "§l" : "";
        String name = plain(material);
        StringBuilder out = new StringBuilder(color).append(bold).append(name);
        if (amount > 1) {
            out.append(" §8× §f").append(amount);
        }
        return out.toString();
    }

    /** Linha de lore com nome do material (referências de preço). */
    public static String loreReference(Material material, String suffix) {
        return accent(material) + plain(material) + " §8» " + suffix;
    }

    public static String buyMessage(int amount, Material material, String pricePart) {
        String qty = amount > 1 ? amount + "× " : "";
        return "§aComprou §f" + qty + accent(material) + plain(material) + " §apor " + pricePart;
    }

    private static String translate(Material material) {
        try {
            Component key = Component.translatable(material.translationKey());
            Component rendered = GlobalTranslator.render(key, PT);
            String name = PLAIN.serialize(rendered);
            if (isValidTranslation(name, material)) {
                return name;
            }
        } catch (Exception ignored) {
        }
        return fallbackEnum(material);
    }

    private static boolean isValidTranslation(String name, Material material) {
        if (name == null || name.isBlank()) return false;
        String low = name.toLowerCase(Locale.ROOT);
        if (low.equals(material.name().toLowerCase(Locale.ROOT))) return false;
        return !low.contains("block.minecraft") && !low.contains("item.minecraft");
    }

    private static String fallbackEnum(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(WORDS_PT.getOrDefault(part, capitalize(part)));
        }
        return sb.toString();
    }

    private static String capitalize(String word) {
        if (word.length() <= 1) return word.toUpperCase(Locale.ROOT);
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private static String accent(Material material) {
        if (material == null) return "§f";
        if (isValuable(material)) return "§b";
        if (isWeapon(material)) return "§c";
        if (material.isEdible()) return "§6";
        if (isTool(material)) return "§3";
        if (Tag.ITEMS_ARROWS.isTagged(material) || material == Material.SPECTRAL_ARROW) return "§e";
        if (material == Material.ENDER_PEARL || material == Material.ENDER_EYE) return "§5";
        if (material.name().contains("POTION") || material == Material.EXPERIENCE_BOTTLE) return "§d";
        return "§f";
    }

    private static boolean isValuable(Material material) {
        String n = material.name();
        return n.contains("DIAMOND") || n.contains("NETHERITE") || n.contains("EMERALD")
                || material == Material.GOLDEN_APPLE || material == Material.ENCHANTED_GOLDEN_APPLE;
    }

    private static boolean isWeapon(Material material) {
        return Tag.ITEMS_SWORDS.isTagged(material)
                || Tag.ITEMS_AXES.isTagged(material)
                || material == Material.BOW
                || material == Material.CROSSBOW
                || material == Material.TRIDENT
                || material == Material.MACE;
    }

    private static boolean isTool(Material material) {
        return Tag.ITEMS_PICKAXES.isTagged(material)
                || Tag.ITEMS_SHOVELS.isTagged(material)
                || Tag.ITEMS_HOES.isTagged(material)
                || material == Material.SHEARS
                || material == Material.FISHING_ROD
                || material == Material.FLINT_AND_STEEL;
    }

    private static String stripColors(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
    }
}
