package com.minecraft.core.bukkit.menu.server.arcade.mode.duels.collectible;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.DuelCollectibleController;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class CollectibleMenu extends Menu {

    private final DuelMember member;
    private final DuelCollectibleType type;

    public CollectibleMenu(Player player, DuelMember member, DuelCollectibleType type, Menu last) {
        super(player, type.getName(), last, 6, 21);

        this.member = member;
        this.type = type;
    }

    @Override
    public void handle() {
        clear();

        Account account = member.getAccount();

        List<DuelCollectible> collectibles = DuelCollectibleController.list(type);

        collectibles.sort((a, b) -> {
            if (a.getName().equalsIgnoreCase("Nenhuma")) return -1;
            if (b.getName().equalsIgnoreCase("Nenhuma")) return 1;

            if (a.getName().equalsIgnoreCase("Aleatório")) return -1;
            if (b.getName().equalsIgnoreCase("Aleatório")) return 1;

            boolean aHasPermission = a.hasPermission(account);
            boolean bHasPermission = b.hasPermission(account);

            if (aHasPermission && bHasPermission)
                return a.getName().compareToIgnoreCase(b.getName());

            if (aHasPermission) return -1;
            if (bHasPermission) return 1;

            return a.getName().compareToIgnoreCase(b.getName());
        });

        buildPageItems(collectibles, 10, (collectible, slot) -> {

            boolean isUsing = member.isActivateCollectible(collectible), hasAccess = collectible.hasPermission(account);

            Item icon = collectible.getIcon().clone()
                    .name((!hasAccess ? "§c" : "§a") + collectible.getName())
                    .flags(ItemFlag.values());

            if (isUsing)
                icon.enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

            List<String> lore = new ArrayList<>(collectible.getLore());

            if (!lore.isEmpty())
                lore.add("");

            if (!collectible.isFree()) {
                StringBuilder builder = new StringBuilder("§7Incluso no ");

                List<RankType> ranks = collectible.getRanks();

                if (ranks.size() == 1) {
                    RankType rank = ranks.get(0);

                    builder.append(rank.getBoldColorName().toUpperCase());
                } else {
                    int index = 1;
                    boolean end = false;
                    for (RankType rank : ranks) {
                        if (index >= ranks.size()) end = true;

                        builder.append(rank.getBoldColorName().toUpperCase()).append("§r").append(end ? "." : ", ");

                        index++;
                    }
                }

                if (lore.isEmpty())
                    lore.add("");

                lore.add(builder.toString());
                lore.add("");
            }

            CollectibleRarity rarity = collectible.getRarity();

            if (rarity != null) {
                lore.add("§7Raridade: " + rarity.getColoredName());
                lore.add("");
            }

            lore.add(isUsing ? "§aJá selecionado." : !hasAccess ? "§cSem acesso." : "§eClique para selecionar!");

            /* Atualizando dados do ícone */
            icon.lore(lore);

            icon.click(event -> {
                if (!hasAccess || isUsing) {
                    sound(MenuSound.ERROR);
                    return;
                }

                member.setCollectible(collectible);

                getPlayer().sendMessage("§eVocê selecionou §6" + collectible.getName() + "§e!");

                sound(MenuSound.SUCCESS);
                handle();
            });

            addItem(slot, icon);
        });

        /* Colecionável atual */
        DuelCollectible current = member.getActiveCollectible(type);

        if (current != null)
            addItem(getTotalSlots() - 5, current.getIcon().clone()
                    .name("§eSelecionado - §6" + current.getName())
                    .flags(ItemFlag.values())
                    .lore(current.getLore()));

        if (isReturnable())
            addBackButton(getTotalSlots() - 6);

        display();
    }
}
