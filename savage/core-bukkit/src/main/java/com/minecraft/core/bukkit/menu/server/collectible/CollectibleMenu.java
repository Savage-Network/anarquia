package com.minecraft.core.bukkit.menu.server.collectible;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CollectibleMenu extends Menu {

    private final Account account;
    private final CollectibleController controller = Core.getCollectibleController();

    public CollectibleMenu(Player player) {
        super(player, "Cosméticos", 5);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        addItem(4, Item.of(Material.NETHER_STAR)
                .name("§6Cosméticos Savage")
                .lore(
                        "§7Personalize sua experiência nos lobbies.",
                        "",
                        "§7Escolha um grupo abaixo para ver",
                        "§7chapéus, pets, auras, trajes e mais.",
                        "",
                        "§fAtivos: §a" + CollectibleOperator.list(getPlayer()).size(),
                        "§fTotal desbloqueados: §e" + totalUnlocked() + "/" + totalCosmetics()
                ));

        String[] groups = {"Aparência", "Efeitos", "Companheiros", "Lobby", "Social"};
        Material[] icons = {
                Material.DIAMOND_HELMET,
                Material.BLAZE_POWDER,
                Material.BONE,
                Material.COMMAND,
                Material.NAME_TAG
        };
        int[] slots = {20, 21, 22, 23, 24};

        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            int slot = slots[i];
            Material icon = icons[i];

            int unlocked = 0;
            int total = 0;
            for (CollectibleCategory cat : CollectibleCategory.byGroup(group)) {
                unlocked += account.getCollectibles(cat).size();
                total += controller.list(cat).size();
            }

            addItem(slot, Item.of(icon)
                    .name("§a" + group)
                    .lore(
                            "§7" + categoryCount(group) + " categorias disponíveis",
                            "",
                            "§fDesbloqueados: §c" + unlocked + "/§c" + total + " §8(" + Util.percentage(unlocked, total) + "%)",
                            "",
                            "§eClique para abrir!"
                    )
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new CollectibleGroupMenu(getPlayer(), group, this).handle();
                    }));
        }

        addCloseButton(40);
        display();
    }

    private int categoryCount(String group) {
        return CollectibleCategory.byGroup(group).size();
    }

    private int totalUnlocked() {
        int sum = 0;
        for (CollectibleCategory c : CollectibleCategory.values())
            sum += account.getCollectibles(c).size();
        return sum;
    }

    private int totalCosmetics() {
        int sum = 0;
        for (CollectibleCategory c : CollectibleCategory.values())
            sum += controller.list(c).size();
        return sum;
    }
}
