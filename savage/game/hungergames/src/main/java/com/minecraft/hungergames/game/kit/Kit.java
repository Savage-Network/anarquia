package com.minecraft.hungergames.game.kit;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public abstract class Kit implements Listener {

    private String name = "Kit";
    private String displayName = "Kit";


    @Setter private RankType requiredRank = RankType.MEMBER;
    @Setter private KitCategory category = KitCategory.DEFAULT;
    @Setter private int price = 0;
    @Setter private boolean enabled = true;

    private final List<String> description = new ArrayList<>();
    private final List<ItemStack> items = new ArrayList<>();


    protected void define(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
    }


    public void register() {
        Bukkit.getPluginManager().registerEvents(this, BukkitCore.getInstance());
        onRegister();
    }


    public void unregister() {
        HandlerList.unregisterAll(this);
        onUnregister();
    }


    protected void onRegister() {}


    protected void onUnregister() {}


    public Kit describe(String... lines) {
        description.addAll(Arrays.stream(lines)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return this;
    }

    public Kit items(ItemStack... stacks) {
        items.clear();
        items.addAll(Arrays.stream(stacks)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return this;
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void give(Player player) {
        if (player == null || items.isEmpty()) return;

        PlayerInventory inv = player.getInventory();
        for (ItemStack stack : items) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            inv.addItem(stack.clone());
        }
        onGiven(player);
    }


    public void remove(Player player) {
        if (player == null || items.isEmpty()) return;

        PlayerInventory inv = player.getInventory();
        for (ItemStack kitItem : items) {
            if (kitItem == null) continue;
            Material mat = kitItem.getType();
            String name = metaName(kitItem);

            for (ItemStack content : inv.getContents()) {
                if (content == null) continue;
                if (content.getType() != mat) continue;
                String contentName = metaName(content);

                // Match por material e nome (se kitItem tiver nome)
                if (name == null || Objects.equals(name, contentName)) {
                    inv.removeItem(content);
                }
            }
        }
    }


    public boolean isKitItem(ItemStack stack) {
        if (stack == null) return false;
        Material mat = stack.getType();
        String name = metaName(stack);

        for (ItemStack kitItem : items) {
            if (kitItem == null) continue;
            if (kitItem.getType() != mat) continue;
            String kitName = metaName(kitItem);

            if (kitName == null || Objects.equals(kitName, name))
                return true;
        }

        return false;
    }

    public void addCooldown(Player player, String action, long seconds, boolean showBar) {
        if (player == null) return;
        String key = cooldownKey(action);

        Cooldown cd = new Cooldown(key, seconds);
        cd.setShowBar(showBar);

        BukkitCore.getManager().getCooldown().addCooldown(player, cd);
    }

    public boolean hasCooldown(Player player, String action) {
        if (player == null) return false;
        String key = cooldownKey(action);
        return BukkitCore.getManager().getCooldown().hasCooldown(player, key);
    }

    public void removeCooldown(Player player, String action) {
        if (player == null) return;
        String key = cooldownKey(action);
        BukkitCore.getManager().getCooldown().removeCooldown(player, key);
    }


    public void setCombat(Player player, long seconds) {
        addCooldown(player, "combat", seconds, false);
    }

    public boolean inCombat(Player player) {
        return hasCooldown(player, "combat");
    }


    protected String cooldownKey(String action) {
        return "kit-" + name.toLowerCase() + "-" + action.toLowerCase();
    }


    public boolean hasRequiredRank(Account account) {
        if (requiredRank == null || account == null) return true;
        return account.hasAvailableRank(requiredRank);
    }


    public void onSelect(Player player) {
        give(player);
    }


    protected void onGiven(Player player) {}


    public static ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null && lore.length > 0)
                meta.setLore(Arrays.stream(lore).filter(Objects::nonNull).collect(Collectors.toList()));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String metaName(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null;
    }
}
