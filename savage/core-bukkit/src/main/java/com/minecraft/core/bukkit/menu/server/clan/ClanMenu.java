package com.minecraft.core.bukkit.menu.server.clan;

import com.minecraft.core.api.clan.Clan;
import com.minecraft.core.api.clan.metadata.emblem.ClanEmblem;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClanMenu extends Menu {

    private final Clan clan;

    public ClanMenu(Player player, Clan clan, Menu last) {
        super(player, "Clan - " + clan.getName(), last, 5);

        this.clan = clan;
    }

    @Override
    public void handle() {
        clear();

        addItem(13, Item.of(Material.PAINTING, "§a" + clan.getName(),
                "§7Tag: §f" + clan.getTag(),
                "§7Membros: §f" + clan.getMembers().size(),
                "",
                "§7Criado em: §f" + DateUtil.getSimpleDate(clan.getMetadata().getCreatedAt())));

        /* Emblem System */
        List<String> emblemLore = new ArrayList<>(Arrays.asList(
                "§7Destaque-se com os emblemas",
                "§7exclusivos do clan.",
                ""
        ));

        StringBuilder builder = new StringBuilder();

        builder.append("§7Emblemas: ");

        for (ClanEmblem emblem : ClanEmblem.values()) {
            if (!emblem.isAchievable()) continue;

            builder.append(clan.hasEmblem(emblem) ? emblem.getColor() : "§7").append("✸");
        }

        emblemLore.add(builder.toString());
        emblemLore.add("");

        addItem(29, Item.of(Material.SKULL_ITEM, 3, "§aEmblemas", emblemLore)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmFkOGEzYTNiMzZhZGQ1ZDk1NDFhOGVjOTcwOTk2ZmJkY2RlYTk0MTRjZDc1NGM1MGU0OGU1ZDM0ZjFiZjYwYSJ9fX0="));

        if (isReturnable())
            addBackButton();

        display();
    }
}
