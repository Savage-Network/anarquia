package com.minecraft.arcade.bedwars.menu.shop;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.arcade.bedwars.user.context.UserContext;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.custom.ShopConfig;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.enums.item.flag.BedItemFlag;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.menu.type.BedMenuType;
import com.minecraft.core.Core;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShopMenu extends Menu {

    private final User user;

    private final BedMember member;
    private final Arena arena;

    private final Team team;

    @Setter
    private BedShop shop;

    public ShopMenu(Player player, BedShop shop) {
        super(player, "Loja: " + shop.getName(), 6, 21);

        this.user = (User) User.of(player.getUniqueId());

        this.member = user.getMember();
        this.arena = user.getArena();

        this.team = user.getTeam();

        this.shop = shop;

        setAllowClickItemWithQuantity(true);
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível recolher os dados...");
        else {

            int shopSlot = 0;
            for (BedShop shop : BedShop.values()) {
                boolean activeIcon = this.shop.equals(shop);

                addItem(shopSlot, Item.fromStack(shop.getIcon())
                        .name((activeIcon ? "§c" : "§a") + shop.getName())
                        .lore(activeIcon ? "§cVisualizando." : "§eClique para ver!")
                        .click(event -> {
                            if (this.shop.equals(shop)) {
                                sound(MenuSound.ERROR);
                                return;
                            }

                            setShop(shop);
                            setTitle("Loja: " + shop.getName());

                            sound(MenuSound.PAGINATED);
                            handle();
                        }));

                shopSlot++;
            }

            // Adicionar vidros abaixo dos ícones de lojas
            for (int i = 9; i < 18; i++)
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, "§8↑ Categorias", "§8↓ Itens"));

            // Renderizando itens da loja
            handleItemList();
            
            // Botão do editor de hotbar no último slot
            addItem(53, Item.of(Material.BLAZE_POWDER, "§aEditor de hotbar",
                            "§7Altere as predefinições",
                            "§7da hotbar.",
                            "",
                            "§eClique para ver mais.",
                            "§cminecraft:blaze_powder",
                            "§cNBT: 1 tag(s)")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new com.minecraft.arcade.bedwars.menu.shop.HotbarEditorShopMenu(getPlayer(), member, this).handle();
                    }));
        }

        display();
    }

    protected void handleItemList() {
        if (!shop.equals(BedShop.FAVORITE)) {
            // Renderizar inventário da loja

            List<BedWarsItem> list = BedWarsItem.list(shop)
                    .stream().filter(item -> item.isAvailableForMode(arena.getType()))
                    .collect(Collectors.toList());

            if (list.isEmpty())
                addErrorButton(31, "§cNão há itens disponíveis...");
            else
                buildPageItems(list, 19, (item, slot) -> addItem(slot, buildItem(item)));

        } else {
            // Renderizar inventário de favoritos

            // Moldar espaços vázios da loja
            int emptySlot = 19, lastEmptySlot = emptySlot;
            for (int i = 0; i < getMaxItems(); i++) {

                addItem(emptySlot, Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                        "§7Este é um espaço para",
                        "§7item favorito."));

                emptySlot++;
                if (emptySlot == (lastEmptySlot + 7)) {
                    emptySlot += 2;
                    lastEmptySlot = emptySlot;
                }
            }

            List<BedItem> favorites = member.getMenu(BedMenuType.FAVORITE).getItemList();

            for (BedItem favorite : favorites) {
                BedWarsItem item = BedWarsItem.of(favorite.getName());

                if (item == null) continue;

                if (!item.isAvailableForMode(arena.getType()))
                    addItem(favorite.getSlot(), Item.of(Material.STAINED_GLASS_PANE, 14, "§cItem incompatível!",
                            "§7Este espaço está ocupado",
                            "§7por um item incompatível."));
                else
                    addItem(favorite.getSlot(), buildItem(item));

            }
        }
    }

    protected Item buildItem(BedWarsItem item) {
        // Verificar se é sala customizada e obter configurações
        boolean isCustomRoom = arena.getCustom() != null;
        ShopConfig.ItemConfig config = isCustomRoom ? arena.getCustom().getShopConfig().getItemConfig(item) : null;
        boolean freeShop = isCustomRoom && arena.getCustom().getShopConfig().isFreeShop();
        
        BedOre ore = config != null ? config.getOre() : item.getOre();
        int price = config != null ? config.getPrice() : item.getPrice();
        int quantity = config != null ? config.getQuantity() : item.getStack().getAmount();

        UserContext context = user.getContext();

        Item model = Item.fromStack(item.getStack())
                .amount(quantity)
                .flags(ItemFlag.values());

        List<String> lore = new ArrayList<>(Collections.singletonList(
                freeShop ? "§7Preço: §aGRÁTIS" : "§7Preço: " + ore.getValue(price)
        ));

        if (!item.getLore().isEmpty()) {
            lore.add("");
            lore.addAll(item.getLore());
        }

        lore.add("");
        lore.add("§eClique com §bShift§e para " + (member.isFavoriteItem(item) ? "desfavoritar" : "favoritar") + "!");

        String titleColor, stateLore;

        if (item.hasFlag(BedItemFlag.CAN_BUY_AFTER_BED_BREAK) && arena.hasTeamsWithBed()) {
            titleColor = "§c";
            stateLore = "§cDisponível apenas quando não há camas vivas.";
        } else {
            boolean canBuy = freeShop || user.canBuyItem(item, ore, price);
            titleColor = canBuy ? "§a" : "§c";

            stateLore = user.hasArmor(item) ? "§cArmadura comprada."
                    : user.hasTool(item) || context.isPurchasedShears() && item.equals(BedWarsItem.TOOLS_SHEARS)
                    ? "§cFerramenta comprada."
                    : item.equals(BedWarsItem.UTIL_TRACKING) && context.getTracker().isActive()
                    ? "§cRastreador adquirido."
                    : item.equals(BedWarsItem.LOJA_PORTATIL) && context.isPurchasedPortableShop()
                    ? "§cLoja portátil já adquirida."
                    : !canBuy
                    ? "§cVocê não possui " + ore.getName() + " suficientes."
                    : "§eClique para comprar!";
        }

        lore.add(stateLore);

        return model.name(titleColor + item.getName())
                .lore(lore)
                .click(event -> {
                    if (event.isShiftClick()) {
                        sound(MenuSound.PAGINATED);

                        // Abrir seleção de favoritos
                        if (!member.isFavoriteItem(item)) {
                            new FavoriteShopMenu(getPlayer(), item, this).handle();
                        } else {
                            // Excluir item dos favoritos
                            member.removeFavoriteItem(item);

                            handle();
                        }

                        return;
                    }

                    // Lidar com a compra do item
                    if (item.hasFlag(BedItemFlag.CAN_BUY_AFTER_BED_BREAK) && arena.hasTeamsWithBed()) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê não pode comprar " + item.getName() + " com camas vivas.");
                        return;
                    }

                    if (item.equals(BedWarsItem.UTIL_TRACKING) && context.getTracker().isActive()) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já adquiriu o Rastreador.");
                        return;
                    }

                    if (item.equals(BedWarsItem.LOJA_PORTATIL) && context.isPurchasedPortableShop()) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já adquiriu a Loja Portátil.");
                        return;
                    }

                    // Comprar Ovo de Pontes somente na Esmeralda II
//                    if (arena.isType(Type.COMPETITIVE) && item.equals(BedWarsItem.UTIL_EGG) && arena.getGeneratorsLevel(BedOre.EMERALD) != GeneratorLevel.TWO) {
//                        sound(MenuSound.ERROR);
//
//                        getPlayer().sendMessage("§cO " + item.getName() + " só pode ser adquirido na Esmeralda II.");
//                        return;
//                    }

                    if (!freeShop && !user.canBuyItem(item, ore, price)) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê não tem " + ore.getName().toLowerCase() + " suficientes.");
                        return;
                    }

                    if (context.isPurchasedShears() && item.equals(BedWarsItem.TOOLS_SHEARS)) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já adquiriu a " + item.getName() + "!");
                        return;
                    }

                    PlayerInventory inv = getPlayer().getInventory();

                    Item stack = Item.fromStack(item.getStack()).amount(quantity);

                    if (item.isType(BedShop.COMBAT) || item.isType(BedShop.ARMOR) || item.isType(BedShop.TOOLS)
                            || item.isType(BedShop.ARTILLERY) || item.equals(BedWarsItem.TOOLS_SHEARS))
                        stack.unbreakable();

                    if (item.isType(BedShop.ARMOR)) {
                        if (!handleArmorPurchase(inv, item))
                            return;
                    } else if (item.isToolPickaxe() || item.isToolAxe())
                        handleToolPurchase(item);
                    else if (isSword(stack.getType())) {
                        // Verificar se está tentando comprar uma espada inferior
                        BedWarsItem currentSword = context.getSwordItem();
                        if (currentSword != null && item.ordinal() < currentSword.ordinal()) {
                            sound(MenuSound.ERROR);
                            getPlayer().sendMessage("§cVocê já possui uma espada superior!");
                            return;
                        }

                        if (BukkitUtil.hasItemInInventory(getPlayer(), Material.WOOD_SWORD))
                            BukkitUtil.replaceItemByType(getPlayer(), Material.WOOD_SWORD, stack);
                        else
                            inv.addItem(stack);

                        // Salvar a espada comprada
                        context.setSwordItem(item);

                        // Encantar espadas de forma síncrona
                        if (team.hasSword()) {
                            Core.getPlatform().runSync(() -> {
                                team.getSword().applyEnchantment(getPlayer());
                            });
                        }
                    } else if (item.isColored()) {
                        int hexColor = ColorUtil.getIdByColor(user.getTeam().getColor());

                        ItemStack itemStack = BukkitUtil.getItemByType(getPlayer(), stack.getType());

                        int itemAmount = itemStack != null ? itemStack.getAmount() + stack.getAmount() : stack.getAmount();

                        stack.durability(hexColor);

                        if (itemAmount <= inv.getMaxStackSize()) {
                            stack.setAmount(itemAmount);

                            BukkitUtil.replaceItemByType(getPlayer(), stack.getType(), stack);
                        } else
                            inv.addItem(stack);

                    } else {

                        /* Setando a tesoura como comprada */
                        if (item.equals(BedWarsItem.TOOLS_SHEARS) && !context.isPurchasedShears())
                            context.setPurchasedShears(true);

                        if (item.equals(BedWarsItem.UTIL_TRACKING) && !context.getTracker().isActive())
                            context.getTracker().setActive(true);

                        if (item.equals(BedWarsItem.LOJA_PORTATIL) && !context.isPurchasedPortableShop())
                            context.setPurchasedPortableShop(true);

                        // Verificar se o item pertence a uma categoria configurada na hotbar
                        int targetSlot = -1;
                        for (int slot = 0; slot < 9; slot++) {
                            BedShop configuredShop = member.getMetadata().getHotbarSlot(slot);
                            if (configuredShop != null && item.isType(configuredShop)) {
                                targetSlot = slot;
                                break;
                            }
                        }
                        
                        // Se encontrou um slot configurado, colocar o item lá
                        if (targetSlot != -1) {
                            ItemStack existingItem = inv.getItem(targetSlot);
                            
                            // Se o slot já tem um item do mesmo tipo, somar a quantidade
                            if (existingItem != null && existingItem.getType() == stack.getType() && 
                                existingItem.getDurability() == stack.getDurability()) {
                                int newAmount = existingItem.getAmount() + stack.getAmount();
                                if (newAmount <= existingItem.getMaxStackSize()) {
                                    existingItem.setAmount(newAmount);
                                } else {
                                    existingItem.setAmount(existingItem.getMaxStackSize());
                                    // Adicionar o resto no inventário
                                    stack.setAmount(newAmount - existingItem.getMaxStackSize());
                                    inv.addItem(stack);
                                }
                            } else {
                                // Se o slot tem um item diferente, tentar adicionar o item antigo primeiro
                                if (existingItem != null) {
                                    // Verificar se há espaço no inventário para o item antigo
                                    java.util.HashMap<Integer, ItemStack> leftover = inv.addItem(existingItem);
                                    
                                    // Se não conseguiu adicionar o item antigo (inventário cheio)
                                    if (!leftover.isEmpty()) {
                                        // Não substituir, adicionar o novo item normalmente
                                        inv.addItem(stack);
                                        sound(MenuSound.ERROR);
                                        getPlayer().sendMessage("§cSeu inventário está cheio! O item foi adicionado em outro slot.");
                                        
                                        // Efetuando a compra do item
                                        if (!freeShop) {
                                            user.removeOre(ore, price);
                                        }
                                        handle();
                                        getPlayer().sendMessage("§eO item §6" + item.getName() + "§e foi adquirido.");
                                        return;
                                    }
                                }
                                // Se conseguiu mover o item antigo ou o slot estava vazio, colocar o novo
                                inv.setItem(targetSlot, stack);
                            }
                        } else {
                            // Se não tem slot configurado, adicionar normalmente
                            inv.addItem(stack);
                        }
                    }

                    // Efetuando a compra do item
                    sound(MenuSound.DONE);
                    if (!freeShop) {
                        user.removeOre(ore, price);
                    }

                    handle();

                    getPlayer().sendMessage("§eO item §6" + item.getName() + "§e foi adquirido.");
                });
    }

    protected boolean handleArmorPurchase(PlayerInventory inv, BedWarsItem item) {
        if (user.hasArmor(item)) {
            sound(MenuSound.ERROR);

            getPlayer().sendMessage("§cA armadura " + item.getName() + " já foi adquirida.");
            return false;
        }

        String name = item.name().split("_")[1].toUpperCase();

        inv.setLeggings(Item.of(Material.getMaterial(name + "_LEGGINGS")).unbreakable());
        inv.setBoots(Item.of(Material.getMaterial(name + "_BOOTS")).unbreakable());

        user.getContext().setArmorItem(item);

        // Aplicar encantamentos de armadura de forma síncrona
        if (team.hasArmor()) {
            Core.getPlatform().runSync(() -> {
                team.getArmor().applyEnchantment(getPlayer());
            });
        }

        return true;
    }

    protected void handleToolPurchase(BedWarsItem item) {
        if (item.isToolPickaxe()) {
            if (item.isInferior(user.getContext().getPickaxeItem())) {
                sound(MenuSound.ERROR);

                getPlayer().sendMessage("§cVocê já possui uma picareta superior!");
                return;
            }

            Material pickaxe = user.getContext().getPickaxeItem() == null ? Material.WOOD_PICKAXE : user.getContext().getPickaxeItem().getStack().getType();

            BukkitUtil.replaceItemByType(getPlayer(), pickaxe, Item.fromStack(item.getStack()).unbreakable());

            user.getContext().setPickaxeItem(item);
        } else if (item.isToolAxe()) {
            if (item.isInferior(user.getContext().getAxeItem())) {
                sound(MenuSound.ERROR);

                getPlayer().sendMessage("§cVocê já possui um machado superior!");
                return;
            }

            Material axe = user.getContext().getAxeItem() == null ? Material.WOOD_AXE : user.getContext().getAxeItem().getStack().getType();

            BukkitUtil.replaceItemByType(getPlayer(), axe, Item.fromStack(item.getStack()).unbreakable());

            user.getContext().setAxeItem(item);
        }
    }

    private static boolean isSword(Material material) {
        return material.name().endsWith("_SWORD");
    }
}
