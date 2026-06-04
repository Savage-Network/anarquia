package com.minecraft.core.bukkit.menu.server.arcade.editor;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.Member;
import com.minecraft.core.member.context.menu.MenuMetadata;
import com.minecraft.core.util.list.serialization.Serialization;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArcadeEditingMenu extends Menu {

    private Member member;
    private final ArcadeCategory arcade;

    public ArcadeEditingMenu(Player player, ArcadeCategory arcade, Menu last) {
        super(player, "Editar: " + arcade.getName(), last, 6);

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account != null)
            this.member = account.loadMember(arcade.getServer());

        this.arcade = arcade;

        /* Configurando Inventário */
        setAllowClick(true);
        setAllowShift(false);
        setAllowDrag(false);

        addProtectedContent(45, 46, 47, 51, 52, 53);
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível carregar sua conta...");
        else {
            String base64 = member.getBase64(arcade);

            String[] base64split = base64.split(" @ ");

            if (base64split.length <= 1)
                addErrorButton("§cNão foi possível ver o inventário...");
            else {
                MenuMetadata data = member.getMenu(arcade);

                String internalData = base64split[0], hotbarData = base64split[1];

                List<ItemStack> hotbar = Serialization.itemStackArrayFromBase64(hotbarData);

                int slot = 36;
                for (ItemStack content : hotbar) {
                    if (slot > 44) return;

                    if (content == null)
                        content = new ItemStack(Material.AIR);

                    Item item = Item.of(content.getType())
                            .amount(content.getAmount())
                            .durability(content.getDurability());

                    item.updateMeta(content.getItemMeta());

                    addItem(slot, item);
                    slot++;
                }

                List<ItemStack> internal = Serialization.itemStackArrayFromBase64(internalData);

                slot = 0;
                for (ItemStack content : internal) {
                    if (slot > 26) return;

                    if (content == null) {
                        content = new ItemStack(Material.AIR);
                    }

                    Item item = Item.of(content.getType())
                            .amount(content.getAmount())
                            .durability(content.getDurability());

                    item.updateMeta(content.getItemMeta());

                    addItem(slot, item);
                    slot++;
                }

                /* Adicionar botão de reset / save */
                addItem(getTotalSlots() - 6, Item.of(Material.STAINED_CLAY, 14, "§cRedefinir",
                                "§7Redefina o seu inventário",
                                "§7para o padrão.")
                        .click(event -> {
                            String snapshot = data.getBase64Snapshot();

                            if (snapshot.equalsIgnoreCase(base64)) {
                                sound(MenuSound.ERROR);
                                getPlayer().sendMessage("§cVocê não pode redefinir o seu inventário.");
                                return;
                            }

                            member.updateMenu(arcade, snapshot);

                            sound(MenuSound.SUCCESS);
                            handle();
                        }), true);

                addItem(getTotalSlots() - 4, Item.of(Material.STAINED_CLAY, 5, "§aSalvar alterações",
                                "§7Salve as alterações",
                                "§7realizadas no inventário.")
                        .click(event -> {
                            InventoryView view = event.getView();

                            List<ItemStack> items = Arrays.stream(view.getTopInventory().getContents()).collect(Collectors.toList());

                            List<ItemStack> top = items.subList(0, 27);

                            String topData = Serialization.itemStackArrayToBase64(top);

                            List<ItemStack> below = items.subList(36, 45);

                            String belowData = Serialization.itemStackArrayToBase64(below);

                            String newBase64 = topData + " @ " + belowData;

                            if (newBase64.equalsIgnoreCase(base64)) {
                                sound(MenuSound.ERROR);
                                getPlayer().sendMessage("§cVocê não pode salvar o seu inventário.");
                            } else {
                                close();

                                member.updateMenu(arcade, newBase64);

                                sound(MenuSound.SUCCESS);
                                getPlayer().sendMessage("§aVocê salvou o seu inventário.");
                            }
                        }), true);
            }
        }

        /* Adiciona uma fileira de STAINED_GLASS_PANE na antepenúltima fileira */
        for (int i = 0; i < 9; i++) {
            addItem((getRows() - 3) * 9 + i, Item.of(Material.STAINED_GLASS_PANE, 0, " "), true);
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
