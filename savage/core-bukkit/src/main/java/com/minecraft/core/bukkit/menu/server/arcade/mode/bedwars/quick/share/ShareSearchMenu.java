package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.share;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.FavoriteItemShare;
import com.minecraft.core.server.type.ServerType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ShareSearchMenu extends Menu {

    private final List<Account> sharedAccounts;
    private final List<BedItem> favoriteItems;

    public ShareSearchMenu(Player player, List<BedItem> favoriteItems, Menu last) {
        super(player, "Compartilhar favoritos", last, 6, 21);

        Account sender = Core.getAccountData().of(player.getUniqueId());

        this.sharedAccounts = Core.getAccountController()
                .filter(account -> {
                    if (account == null || account.getId().equals(player.getUniqueId())) return false;

                    BedMember member = (BedMember) account.loadMember(ServerType.BEDWARS);

                    if (member == null) return false;

                    FavoriteItemShare share = FavoriteItemShare.of(player.getUniqueId(), account.getId());

                    if (share != null) return false;

                    /* Verificando estado de compartilhamento */
                    if (sender != null && !member.checkShare(sender)) return false;

                    Player target = account.player();

                    return target != null && target.getWorld().equals(player.getWorld());
                });

        this.favoriteItems = favoriteItems;
    }

    @Override
    public void handle() {
        clear();

        if (sharedAccounts.isEmpty())
            addErrorButton("§cNão há jogadores para compartilhar...");
        else {

            buildPageItems(sharedAccounts, 10, (account, slot) -> {

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + account.getNickname(),
                                "§eClique para compartilhar",
                                "§eseus favoritos.")
                        .skullByBase64(account.getSkin().getValue())
                        .click(event -> {
                            close();
                            sound(MenuSound.DONE);

                            new FavoriteItemShare(getPlayer().getUniqueId(), account.getId(), favoriteItems);

                            getPlayer().sendMessage("§eOs favoritos foram compartilhados com §6" + account.getNickname() + "§e!");

                            TextComponent message = new TextComponent("\n§6" + getPlayer().getName() + "§e compartilhou os favoritos com você.");

                            TextComponent open = new TextComponent("§a§lCLIQUE AQUI");

                            open.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/share " + getPlayer().getUniqueId()));

                            message.addExtra("\n");
                            message.addExtra(open);
                            message.addExtra("§e para visualizar.");
                            message.addExtra("\n");

                            account.send(message);
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
