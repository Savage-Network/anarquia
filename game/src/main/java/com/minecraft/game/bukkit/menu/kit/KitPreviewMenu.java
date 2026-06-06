package com.minecraft.game.bukkit.menu.kit;

import com.minecraft.game.bukkit.kit.KitDefinition;
import com.minecraft.game.bukkit.kit.KitSlotItem;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class KitPreviewMenu extends Menu {

    private static final int SLOT_HELMET = 36;
    private static final int SLOT_CHEST = 37;
    private static final int SLOT_LEGS = 38;
    private static final int SLOT_BOOTS = 39;
    private static final int SLOT_INFO = 45;
    private static final int SLOT_BACK = 49;

    private final KitDefinition kit;

    public KitPreviewMenu(Player player, KitDefinition kit, Menu parent) {
        super(player, MenuTexts.format(player, "menus.kit.preview-title",
                MenuTexts.map("kit", kit.getDisplayName().replace('&', '§'))), 6, parent);
        this.kit = kit;
    }

    @Override
    protected boolean bordered() {
        return false;
    }

    @Override
    public void build() {
        clear();
        fillDecorativeGlass();

        for (KitSlotItem item : kit.getItems()) {
            set(item.slot(), MenuItem.fromStack(item.stack().clone()));
        }

        placeArmor(SLOT_HELMET, kit.armorPiece("helmet"));
        placeArmor(SLOT_CHEST, kit.armorPiece("chestplate"));
        placeArmor(SLOT_LEGS, kit.armorPiece("leggings"));
        placeArmor(SLOT_BOOTS, kit.armorPiece("boots"));

        set(SLOT_INFO, MenuItem.of(Material.BOOK)
                .name("§6§l" + kit.getDisplayName().replace('&', '§'))
                .lore(
                        MenuTexts.legacy(player, "menus.kit.preview-desc"),
                        MenuTexts.legacy(player, "menus.kit.preview-desc-2"),
                        "",
                        MenuTexts.format(player, "menus.kit.preview-total",
                                MenuTexts.map("count", String.valueOf(kit.contentCount()))),
                        kit.isClearInventory()
                                ? MenuTexts.legacy(player, "menus.kit.preview-clear-yes")
                                : MenuTexts.legacy(player, "menus.kit.preview-clear-no")
                ));

        back(SLOT_BACK);
    }

    private void placeArmor(int slot, org.bukkit.inventory.ItemStack stack) {
        if (stack == null) return;
        set(slot, MenuItem.fromStack(stack));
    }

    private void fillDecorativeGlass() {
        MenuItem glass = MenuItem.of(Material.GRAY_STAINED_GLASS_PANE).name(" ");
        for (int slot = 40; slot < 54; slot++) {
            if (slot == SLOT_INFO || slot == SLOT_BACK) continue;
            set(slot, glass);
        }
    }
}
