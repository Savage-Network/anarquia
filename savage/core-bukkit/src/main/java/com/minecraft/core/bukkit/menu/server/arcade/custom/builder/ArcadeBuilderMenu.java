package com.minecraft.core.bukkit.menu.server.arcade.custom.builder;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.backend.database.redis.message.types.route.RoomRequestMessage;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.arcade.ArcadeConfig;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.manager.list.ArcadeConfigManager;
import com.minecraft.core.bukkit.menu.server.arcade.custom.builder.map.ArcadeMapSelectMenu;
import com.minecraft.core.bukkit.menu.server.arcade.custom.builder.preference.ArcadePreferenceMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcadeBuilderMenu extends Menu {

    private final transient ArcadeConfigManager manager = BukkitCore.getManager().getArcadeConfig();

    private final Account account;

    private final ArcadeCategory arcade;
    private final Slot slot;

    public ArcadeBuilderMenu(Player player, ArcadeCategory arcade, Slot slot, Menu last) {
        super(player, "Painel: " + arcade.getServer().getName() + " " + arcade.getName(), last, 5);

        this.account = Core.getAccountController().of(player.getUniqueId());

        this.arcade = arcade;
        this.slot = slot;

        manager.create(ArcadeConfig.builder()
                .creator(player.getUniqueId())
                .arcade(arcade)
                .slot(slot)
                .build());
    }

    @Override
    public void handle() {
        clear();

        ArcadeConfig config = manager.of(account.getId());

        if (config == null || config.getPayload() == null)
            addErrorButton("§cNão foi possível carregar os dados do jogo...");
        else {
            ArcadePayload payload = config.getPayload();

            addItem(11, Item.of(Material.getMaterial(arcade.getIconId()),
                            "§a" + arcade.getServer().getName() + " " + arcade.getName())
                    .amount(slot.ordinal())
                    .flags(ItemFlag.values()));

            addItem(13, handleMapList(config));

            addItem(15, Item.of(Material.DIODE, "§aConfigurações",
                            "§7Customize as configurações",
                            "§7das salas.",
                            "",
                            "§eClique para editar!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new ArcadePreferenceMenu(getPlayer(), config, this).handle();
                    }));

            addItem(31, Item.of(Material.SKULL_ITEM, 3, "§aPronto!",
                            "§7Clique para iniciar",
                            "§7a sua sala customizada.",
                            "",
                            "§eClique para criar!")
                    .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjUzOTBlM2NjMWYwNzQ0MWU5ZGUxMzk4NDBkMDgyMTdmODYxODU3ZmY5OGY0MjExYzc5ZjNjMGI0ODYwZDgwZSJ9fX0=")
                    .click(event -> {
                        close();
                        sound(MenuSound.DONE);

                        List<Map> mapList = new ArrayList<>(config.getMaps());

                        if (mapList.isEmpty())
                            mapList.add(payload.getMaps().get(0));

                        config.setProperty("custom", true);

                        config.setProperty("author", account.getId());
                        config.setProperty("mapList", Core.GSON.toJson(mapList));

                        account.send("§aSolicitando sala...");

                        RoomRequestMessage request = new RoomRequestMessage(account.getId(), arcade, slot, Type.CUSTOM, mapList.get(0).getId());

                        request.getProperties().putAll(config.getProperties());

                        request.send();

                        manager.remove(account.getId());
                    }));
        }

        if (isReturnable())
            addItem(getTotalSlots() - 5, Item.of(Material.ARROW, "§aVoltar",
                            getLast() != null ? "§7Para " + getLast().getInitialTitle() : "")
                    .click(event -> {
                        if (getLast() == null) {
                            sound(MenuSound.ERROR);
                            return;
                        }

                        manager.remove(account.getId());
                        getLast().handle();

                        sound(MenuSound.PAGINATED);
                    }), true);

        display();
    }

    protected Item handleMapList(ArcadeConfig config) {
        Item item = Item.of(Material.EMPTY_MAP, "§eMapas");

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Selecione os mapas",
                "§7disponíveis nas partidas.",
                ""
        ));

        List<Map> mapList = config.getMaps();

        if (!mapList.isEmpty()) {

            if (mapList.size() == 1)
                lore.add("§7Selecionado: §a" + mapList.get(0).getName());
            else {
                lore.add("§7Selecionados:");

                if (mapList.size() > 5) {
                    List<Map> topFiveMaps = mapList.subList(0, 5);

                    topFiveMaps.forEach(map -> lore.add("§8" + Constant.ARROW_ALT_SYMBOL + " §6" + map.getName()));

                    int remainingMaps = mapList.size() - 5;

                    lore.add(" §b+" + remainingMaps + "§7 mapas...");
                } else
                    mapList.forEach(map -> lore.add("§8" + Constant.ARROW_ALT_SYMBOL + " §6" + map.getName()));
            }

            lore.add("");
        }

        lore.add("§aClique para editar!");

        return item.lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ArcadeMapSelectMenu(getPlayer(), config, this).handle();
                });
    }
}
