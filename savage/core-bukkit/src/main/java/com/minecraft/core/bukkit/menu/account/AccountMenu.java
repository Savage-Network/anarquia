package com.minecraft.core.bukkit.menu.account;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.account.friend.FriendMenu;
import com.minecraft.core.bukkit.menu.account.medal.MedalMenu;
import com.minecraft.core.bukkit.menu.account.pluscolor.EmberPlusColorMenu;
import com.minecraft.core.bukkit.menu.account.skin.library.SkinLibraryCategoryMenu;
import com.minecraft.core.bukkit.menu.account.stats.StatsMenu;
import com.minecraft.core.bukkit.menu.account.preference.PreferenceMenu;
import com.minecraft.core.bukkit.menu.server.clan.ClanMenu;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AccountMenu extends Menu {
    private final Account account;

    public AccountMenu(Player player) {
        super(player, "Seu perfil", 4);

        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        addItem(13, Item.of(Material.SKULL_ITEM, 3, "§a" + account.getName(),
                        "§7Rank: " + account.getRank().getColoredName(),
                        "§7Primeiro login: " + DateUtil.getSimpleDate(account.getContext().getCreatedAt()),
                        "§7Último login: " + DateUtil.getSimpleDate(account.getContext().getLastLogin()))
                .skullByBase64(account.getSkin().getValue()));

        addItem(19, Item.of(Material.ITEM_FRAME, "§aSkins")
                .click(event -> {
                    new SkinLibraryCategoryMenu(getPlayer(), this).handle();
                }));

        addItem(20, Item.of(Material.DOUBLE_PLANT, "§aMedalhas disponíveis")
                .click(event -> {
                    new MedalMenu(getPlayer(), this).handle();
                }));

        addItem(21, Item.of(Material.NAME_TAG, "§aTags disponíveis")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    TagManager.sendTags(account);
                    close();
                }));

        addItem(22, Item.of(Material.REDSTONE_COMPARATOR, "§aPreferências")
                .click(event -> new PreferenceMenu(getPlayer(), this).handle()));

        addItem(23, Item.of(Material.PAPER, "§aEstatísticas")
                .click(event -> new StatsMenu(getPlayer(), account, this).handle()));

        addItem(24, Item.of(Material.COOKIE, "§aAmizades")
                .click(event -> new FriendMenu(getPlayer(), this).handle()));

        if (account.hasRank(RankType.EMBER_PLUS)) {
            addItem(25, Item.of(Material.SKULL_ITEM, 3, "§aCores do +")
                    .skullByBase64(EmberPlusColorMenu.MENU_ICON_BASE64)
                    .click(event -> new EmberPlusColorMenu(getPlayer(), this).handle()));
        }

        display();
    }
}
