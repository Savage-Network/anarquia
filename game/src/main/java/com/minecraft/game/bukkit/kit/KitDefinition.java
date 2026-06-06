package com.minecraft.game.bukkit.kit;

import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.shop.ItemDisplayNames;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
public final class KitDefinition {

    private final String id;
    private final String displayName;
    private final String permission;
    private final RankType minRank;
    private final long cooldownMs;
    private final boolean clearInventory;
    private final List<KitSlotItem> items;
    private final Material helmet;
    private final Material chestplate;
    private final Material leggings;
    private final Material boots;
    private final Material hubIcon;

    public KitDefinition(String id, String displayName, String permission, RankType minRank,
                         long cooldownMs, boolean clearInventory, List<KitSlotItem> items,
                         Material helmet, Material chestplate, Material leggings, Material boots,
                         Material hubIcon) {
        this.id = id;
        this.displayName = displayName;
        this.permission = permission;
        this.minRank = minRank;
        this.cooldownMs = cooldownMs;
        this.clearInventory = clearInventory;
        this.items = List.copyOf(items);
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.hubIcon = hubIcon != null && hubIcon.isItem() ? hubIcon : Material.CHEST;
    }

    public String cooldownKey() {
        return "kit-" + id;
    }

    public Material iconMaterial() {
        if (hubIcon != Material.CHEST) return hubIcon;
        if (!items.isEmpty()) return items.getFirst().stack().getType();
        if (chestplate != null && chestplate != Material.AIR) return chestplate;
        if (helmet != null && helmet != Material.AIR) return helmet;
        return Material.CHEST;
    }

    public int contentCount() {
        int count = items.size();
        if (helmet != null && helmet != Material.AIR) count++;
        if (chestplate != null && chestplate != Material.AIR) count++;
        if (leggings != null && leggings != Material.AIR) count++;
        if (boots != null && boots != Material.AIR) count++;
        return count;
    }

    public List<String> contentSummaryLines(int maxLines) {
        List<String> lines = new ArrayList<>();
        for (KitSlotItem item : items) {
            if (lines.size() >= maxLines) break;
            ItemStack stack = item.stack();
            lines.add("§7" + ItemDisplayNames.plain(stack) + " §8x" + stack.getAmount());
        }
        appendArmorSummary(lines, maxLines);
        return lines;
    }

    private void appendArmorSummary(List<String> lines, int maxLines) {
        if (lines.size() >= maxLines) return;
        List<String> armor = new ArrayList<>();
        if (helmet != null && helmet != Material.AIR) armor.add(ItemDisplayNames.plain(helmet));
        if (chestplate != null && chestplate != Material.AIR) armor.add(ItemDisplayNames.plain(chestplate));
        if (leggings != null && leggings != Material.AIR) armor.add(ItemDisplayNames.plain(leggings));
        if (boots != null && boots != Material.AIR) armor.add(ItemDisplayNames.plain(boots));
        if (armor.isEmpty()) return;
        if (lines.size() < maxLines) {
            lines.add("§7Armadura: §f" + String.join("§7, §f", armor));
        }
    }

    public ItemStack armorPiece(String slot) {
        Material mat = switch (slot) {
            case "helmet" -> helmet;
            case "chestplate" -> chestplate;
            case "leggings" -> leggings;
            case "boots" -> boots;
            default -> Material.AIR;
        };
        return mat != null && mat != Material.AIR ? new ItemStack(mat) : null;
    }

    public void apply(Player player) {
        PlayerInventory inv = player.getInventory();
        if (clearInventory) {
            inv.clear();
        }
        for (KitSlotItem item : items) {
            inv.setItem(item.slot(), item.stack().clone());
        }
        if (helmet != null && helmet != Material.AIR) inv.setHelmet(new ItemStack(helmet));
        if (chestplate != null && chestplate != Material.AIR) inv.setChestplate(new ItemStack(chestplate));
        if (leggings != null && leggings != Material.AIR) inv.setLeggings(new ItemStack(leggings));
        if (boots != null && boots != Material.AIR) inv.setBoots(new ItemStack(boots));
        player.updateInventory();
    }

    public static KitDefinition fromConfig(String id, ConfigurationSection sec) {
        String display = sec.getString("display-name", "&7" + id);
        String perm = sec.getString("permission", "");
        RankType minRank = RankType.MEMBER;
        try {
            minRank = RankType.valueOf(sec.getString("min-rank", "MEMBER").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) { }

        long cooldownMs = Math.max(0L, sec.getLong("cooldown-seconds", 3600L) * 1000L);
        boolean clear = sec.getBoolean("clear-inventory", true);

        List<KitSlotItem> items = new ArrayList<>();
        List<?> list = sec.getList("items");
        if (list != null) {
            for (Object raw : list) {
                if (raw instanceof Map<?, ?> map) {
                    items.add(KitSlotItem.fromMap(map));
                }
            }
        }

        ConfigurationSection armorSec = sec.getConfigurationSection("armor");
        Material helmet = parseArmor(armorSec, "helmet");
        Material chest = parseArmor(armorSec, "chestplate");
        Material legs = parseArmor(armorSec, "leggings");
        Material boots = parseArmor(armorSec, "boots");
        Material hubIcon = parseMaterial(sec.getString("icon-material", ""));

        return new KitDefinition(
                id.toLowerCase(Locale.ROOT),
                display,
                perm,
                minRank,
                cooldownMs,
                clear,
                items,
                helmet,
                chest,
                legs,
                boots,
                hubIcon
        );
    }

    private static Material parseMaterial(String raw) {
        if (raw == null || raw.isBlank()) return Material.CHEST;
        Material mat = Material.matchMaterial(raw.toUpperCase(Locale.ROOT));
        return mat != null && mat.isItem() ? mat : Material.CHEST;
    }

    private static Material parseArmor(ConfigurationSection sec, String key) {
        if (sec == null) return Material.AIR;
        String raw = sec.getString(key, "");
        if (raw.isBlank()) return Material.AIR;
        Material mat = Material.matchMaterial(raw.toUpperCase(Locale.ROOT));
        return mat != null && mat.isItem() ? mat : Material.AIR;
    }
}
