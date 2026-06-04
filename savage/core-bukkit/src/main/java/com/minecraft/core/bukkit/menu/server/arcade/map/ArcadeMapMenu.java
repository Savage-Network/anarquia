package com.minecraft.core.bukkit.menu.server.arcade.map;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.arcade.room.payload.RoomPayload;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.backend.database.redis.message.types.route.RoomRequestMessage;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.server.Server;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArcadeMapMenu extends Menu {

    private final Account account;

    private final ArcadeCategory arcade;
    private final Type type;

    public ArcadeMapMenu(Player player, ArcadeCategory arcade, Type type, Menu last) {
        super(player, "Selecionar mapa", last, 5);

        this.account = Core.getAccountController().of(player.getUniqueId());

        this.arcade = arcade;
        this.type = type;
    }

    @Override
    public void handle() {
        clear();
        ArcadePayload payload = Core.getArcadeData().read(arcade);

        if (payload == null)
            addErrorButton("§cNão foi possível encontrar mapas");
        else {
            List<Map> maps = payload.getMaps();
            List<Integer> favoriteMapIds = account.getFavoriteMaps(arcade.name());

            List<Map> sortedMaps = maps.stream().sorted(Comparator.comparing((Map map) -> !favoriteMapIds.contains(map.getId())).thenComparing(Map::getName)).toList();
            int slot = 10, last = slot;
            for (Map map : sortedMaps) {
                boolean isFavorite = favoriteMapIds.contains(map.getId());
                List<RoomPayload> properties = payload.getRooms(room -> room.getMap().getId() == map.getId() && room.getType().equals(type));

                int availableRooms = (int) properties.stream()
                        .filter(room -> room.getPhase().equals(RoomPhase.WAITING) || room.getPhase().equals(RoomPhase.STARTING))
                        .count();

                boolean request = availableRooms == 0;

                int finalSlot = slot;
                int finalMapId = map.getId();

                List<String> lore = new ArrayList<>();
                lore.add("§8" + map.getArcade().getName());
                lore.add("");
                lore.add("§7Salas:");
                lore.add(" §7Abertas: §a" + availableRooms);
                lore.add(" §7Total: §a" + properties.size());
                lore.add("");
                if (isFavorite) {
                    lore.add("§c* Clique direito para");
                    lore.add("§cremover dos favoritos.");
                    lore.add("");
                    lore.add("§eClique esquerdo para jogar!");
                } else {
                    lore.add("§a* Clique direito para");
                    lore.add("§aadicionar aos favoritos!");
                    lore.add("");
                    lore.add("§eClique esquerdo para jogar!");
                }

                Item item = Item.of(Material.EMPTY_MAP, (isFavorite ? "§6§l✤ " : "§a") + map.getName(), lore.toArray(new String[0]));
                item.setAmount(availableRooms);
                if (isFavorite) {
                    item.enchantment(Enchantment.DURABILITY, 1);
                    item.flags(ItemFlag.HIDE_ENCHANTS);
                }

                addItem(finalSlot, item
                        .updater(view -> {
                            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                            Item currentItem = getContents().get(finalSlot);

                            if (currentItem == null) return;

                            ArcadePayload updatedPayload = Core.getArcadeData().read(arcade);

                            if (updatedPayload == null) return;

                            List<Integer> updatedFavoriteMapIds = account.getFavoriteMaps(arcade.name());
                            boolean updatedIsFavorite = updatedFavoriteMapIds.contains(finalMapId);

                            List<RoomPayload> updatedProperties = updatedPayload.getRooms(room -> room.getMap().getId() == finalMapId && room.getType().equals(type));

                            int updatedAvailableRooms = (int) updatedProperties.stream()
                                    .filter(room -> room.getPhase().equals(RoomPhase.WAITING) || room.getPhase().equals(RoomPhase.STARTING))
                                    .count();

                            List<String> updatedLore = new ArrayList<>();
                            updatedLore.add("§8" + map.getArcade().getName());
                            updatedLore.add("");
                            updatedLore.add("§7Salas:");
                            updatedLore.add(" §7Abertas: §a" + updatedAvailableRooms);
                            updatedLore.add(" §7Total: §a" + updatedProperties.size());
                            updatedLore.add("");
                            if (updatedIsFavorite) {
                                updatedLore.add("§c* Clique direito para");
                                updatedLore.add("§cremover dos favoritos.");
                                updatedLore.add("");
                                updatedLore.add("§eClique esquerdo para jogar!");
                            } else {
                                updatedLore.add("§a* Clique direito para");
                                updatedLore.add("§aadicionar aos favoritos!");
                                updatedLore.add("");
                                updatedLore.add("§eClique esquerdo para jogar!");
                            }

                            String mapName = updatedPayload.getMaps().stream()
                                    .filter(m -> m.getId() == finalMapId)
                                    .findFirst()
                                    .map(Map::getName)
                                    .orElse("Mapa");
                            
                            currentItem.name((updatedIsFavorite ? "§6§l✤ " : "§a") + mapName);
                            currentItem.setAmount(updatedAvailableRooms);
                            currentItem.lore(updatedLore.toArray(new String[0]));
                            
                            if (updatedIsFavorite) {
                                if (!currentItem.getMeta().hasEnchant(Enchantment.DURABILITY)) {
                                    currentItem.enchantment(Enchantment.DURABILITY, 1);
                                }
                                currentItem.flags(ItemFlag.HIDE_ENCHANTS);
                            } else {
                                org.bukkit.inventory.meta.ItemMeta meta = currentItem.getItemMeta();
                                if (meta != null && meta.hasEnchant(Enchantment.DURABILITY)) {
                                    meta.removeEnchant(Enchantment.DURABILITY);
                                    currentItem.setItemMeta(meta);
                                }
                            }

                            view.setItem(finalSlot, currentItem);

                            getPlayer().updateInventory();
                        })
                        .click(event -> {
                            Reconnect reconnect = Core.getReconnectData().of(account.getId());
                            if (reconnect != null) {
                                sound(MenuSound.ERROR);
                                account.send("§cVocê tem uma partida em andamento! Use /reconnect para voltar à sua partida.");
                                close();
                                return;
                            }
                            
                            ClickType clickType = event.getClick();

                            if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
                                account.toggleFavoriteMap(arcade.name(), finalMapId);
                                sound(MenuSound.PAGINATED);
                                handle();
                                return;
                            }

                            close();
                            sound(MenuSound.PAGINATED);

                            Slot slotType = arcade.getSlots().get(0);

                            Party party = account.getParty();

                            if (party != null && !party.isAuthor(account.getId())) {
                                account.send("§cApenas o dono da party pode procurar salas.");
                                return;
                            }

                            if (request) {
                                getPlayer().sendMessage("§aBuscando sala...");
                                new RoomRequestMessage(getPlayer().getUniqueId(), arcade, slotType, type, finalMapId).send();
                            } else {
                                ArcadeRouteContext route = ArcadeRouteContext.builder()
                                        .arcade(arcade)
                                        .slot(slotType)
                                        .type(type)
                                        .mapId(finalMapId)
                                        .join(Join.PLAYER)
                                        .build();

                                Server server = Core.getServerData().of(arcade.getServer());

                                route.setServerId(server != null ? server.getId() : 0);

                                if (party != null)
                                    route.setLink(party.getMembersId());

                                new ArenaSearchMessage(account.getId(), route).send();
                            }
                        }));

                slot++;
                if (slot == (last + 7)) {
                    slot += 2;
                    last = slot;
                }
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}