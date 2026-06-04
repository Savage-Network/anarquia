package com.minecraft.arcade.eggwars.menu.shop;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.team.Team;
import com.minecraft.arcade.eggwars.user.User;
import com.minecraft.arcade.eggwars.user.context.UserContext;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.eggwars.EggMember;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
import com.minecraft.core.member.list.eggwars.objects.enums.EggOre;
import com.minecraft.core.member.list.eggwars.objects.enums.EggShop;
import com.minecraft.core.member.list.eggwars.objects.enums.item.flag.EggItemFlag;
import com.minecraft.core.member.list.eggwars.objects.menu.item.EggItem;
import com.minecraft.core.member.list.eggwars.objects.menu.type.EggMenuType;
import com.minecraft.core.Core;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShopMenu extends Menu {

    private final User user;

    private final EggMember member;
    private final Arena arena;

    private final Team team;

    @Setter
    private EggShop shop;

    public ShopMenu(Player player, EggShop shop) {
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

            // Colocar categorias na coluna esquerda (slots 0, 9, 18, 27, 36, 45)
            int shopSlot = 0;
            for (EggShop shop : EggShop.values()) {
                boolean activeIcon = this.shop.equals(shop);

                // Calcular slot na coluna esquerda: 0, 9, 18, 27, 36, 45...
                int leftColumnSlot = shopSlot * 9;

                addItem(leftColumnSlot, Item.fromStack(shop.getIcon())
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

            // Adicionar vidros na primeira linha (exceto coluna esquerda onde estão as categorias)
            for (int i = 1; i < 9; i++)
                addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, "§8← Categorias", "§8→ Itens"));

            // Renderizando itens da loja
            handleItemList();
        }

        display();
    }

    protected void handleItemList() {
        if (!shop.equals(EggShop.FAVORITE)) {
            // Renderizar inventário da loja

            List<EggWarsItem> list = EggWarsItem.list(shop)
                    .stream().filter(item -> item.isAvailableForMode(arena.getType()))
                    .collect(Collectors.toList());
            
            // Adicionar armaduras customizadas para EggWars
            if (shop.equals(EggShop.ARMOR)) {
                // Adicionar armaduras customizadas: couro e diamante com proteção 2
                // Estas serão tratadas especialmente no click handler
            }

            if (list.isEmpty() && !shop.equals(EggShop.ARMOR)) {
                addErrorButton(31, "§cNão há itens disponíveis...");
            } else {
                // Adicionar peças individuais de armadura primeiro
                if (shop.equals(EggShop.ARMOR)) {
                    addCustomArmorItems();
                    // Não mostrar armaduras antigas (conjuntos completos) - apenas peças individuais
                    // As peças customizadas ocupam slots 19-23 (primeira linha) e 28-32, 37-41, 46-50 (linhas seguintes)
                    // Se houver outros itens normais, começar após as peças customizadas (slot 51+)
                    if (!list.isEmpty()) {
                        buildPageItems(list, 51, (item, slot) -> {
                            // Filtrar apenas itens que não são armaduras
                            if (!item.isType(EggShop.ARMOR)) {
                                addItem(slot, buildItem(item));
                            }
                        });
                    }
                } else {
                    // Adicionar itens normais normalmente
                    if (!list.isEmpty()) {
                        buildPageItems(list, 19, (item, slot) -> addItem(slot, buildItem(item)));
                    }
                }
            }

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

            List<EggItem> favorites = member.getMenu(EggMenuType.FAVORITE).getItemList();

            for (EggItem favorite : favorites) {
                EggWarsItem item = EggWarsItem.of(favorite.getName());

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

    protected Item buildItem(EggWarsItem item) {
        EggOre ore = item.getOre();

        UserContext context = user.getContext();

        Item model = Item.fromStack(item.getStack())
                .flags(ItemFlag.values());

        List<String> lore = new ArrayList<>(Collections.singletonList(
                "§7Preço: " + ore.getValue(item.getPrice())
        ));

        if (!item.getLore().isEmpty()) {
            lore.add("");
            lore.addAll(item.getLore());
        }

        lore.add("");
        lore.add("§eClique com §bShift§e para " + (member.isFavoriteItem(item) ? "desfavoritar" : "favoritar") + "!");

        String titleColor, stateLore;

        if (item.hasFlag(EggItemFlag.CAN_BUY_AFTER_EGG_BREAK) && arena.hasTeamsWithEgg()) {
            titleColor = "§c";
            stateLore = "§cDisponível apenas quando não há camas vivas.";
        } else {
            titleColor = user.canBuyItem(item) ? "§a" : "§c";

            stateLore = user.hasArmor(item) ? "§cArmadura comprada."
                    : user.hasTool(item) || context.isPurchasedShears() && item.equals(EggWarsItem.TOOLS_SHEARS)
                    ? "§cFerramenta comprada."
                    : item.equals(EggWarsItem.UTIL_TRACKING) && context.getTracker().isActive()
                    ? "§cRastreador adquirido."
                    : item.equals(EggWarsItem.LOJA_PORTATIL) && context.isPurchasedPortableShop()
                    ? "§cLoja portátil já adquirida."
                    : !user.canBuyItem(item)
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
                    if (item.hasFlag(EggItemFlag.CAN_BUY_AFTER_EGG_BREAK) && arena.hasTeamsWithEgg()) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê não pode comprar " + item.getName() + " com camas vivas.");
                        return;
                    }

                    if (item.equals(EggWarsItem.UTIL_TRACKING) && context.getTracker().isActive()) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já adquiriu o Rastreador.");
                        return;
                    }

                    if (item.equals(EggWarsItem.LOJA_PORTATIL) && context.isPurchasedPortableShop()) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já adquiriu a Loja Portátil.");
                        return;
                    }


                    if (!user.canBuyItem(item)) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê não tem " + ore.getName().toLowerCase() + " suficientes.");
                        return;
                    }

                    if (context.isPurchasedShears() && item.equals(EggWarsItem.TOOLS_SHEARS)) {
                        sound(MenuSound.ERROR);

                        getPlayer().sendMessage("§cVocê já adquiriu a " + item.getName() + "!");
                        return;
                    }

                    PlayerInventory inv = getPlayer().getInventory();

                    Item stack = Item.fromStack(item.getStack());

                    if (item.isType(EggShop.COMBAT) || item.isType(EggShop.ARMOR) || item.isType(EggShop.TOOLS)
                            || item.isType(EggShop.ARTILLERY) || item.equals(EggWarsItem.TOOLS_SHEARS))
                        stack.unbreakable();

                    if (item.isType(EggShop.ARMOR)) {
                        if (!handleArmorPurchase(inv, item))
                            return;
                    } else if (item.isToolPickaxe() || item.isToolAxe())
                        handleToolPurchase(item);
                    else if (stack.getType().name().contains("SWORD")) {
                        if (BukkitUtil.hasItemInInventory(getPlayer(), Material.WOOD_SWORD))
                            BukkitUtil.replaceItemByType(getPlayer(), Material.WOOD_SWORD, stack);
                        else
                            inv.addItem(stack);

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
                        if (item.equals(EggWarsItem.TOOLS_SHEARS) && !context.isPurchasedShears())
                            context.setPurchasedShears(true);

                        if (item.equals(EggWarsItem.UTIL_TRACKING) && !context.getTracker().isActive())
                            context.getTracker().setActive(true);

                        if (item.equals(EggWarsItem.LOJA_PORTATIL) && !context.isPurchasedPortableShop())
                            context.setPurchasedPortableShop(true);

                        inv.addItem(stack);
                    }

                    // Efetuando a compra do item
                    sound(MenuSound.DONE);
                    user.removeOre(item);

                    handle();

                    getPlayer().sendMessage("§eO item §6" + item.getName() + "§e foi adquirido.");
                });
    }

    protected boolean handleArmorPurchase(PlayerInventory inv, EggWarsItem item) {
        if (user.hasArmor(item)) {
            sound(MenuSound.ERROR);

            getPlayer().sendMessage("§cA armadura " + item.getName() + " já foi adquirida.");
            return false;
        }

        // Verificar se o jogador não tem nenhuma armadura equipada
        boolean hasNoArmor = inv.getHelmet() == null && inv.getChestplate() == null 
                          && inv.getLeggings() == null && inv.getBoots() == null;

        String name = item.name().split("_")[1].toUpperCase();
        
        // Equipar armadura completa
        Material helmetMaterial, chestplateMaterial, leggingsMaterial, bootsMaterial;
        
        switch (name) {
            case "CHAINMAIL":
                helmetMaterial = Material.CHAINMAIL_HELMET;
                chestplateMaterial = Material.CHAINMAIL_CHESTPLATE;
                leggingsMaterial = Material.CHAINMAIL_LEGGINGS;
                bootsMaterial = Material.CHAINMAIL_BOOTS;
                break;
            case "IRON":
                helmetMaterial = Material.IRON_HELMET;
                chestplateMaterial = Material.IRON_CHESTPLATE;
                leggingsMaterial = Material.IRON_LEGGINGS;
                bootsMaterial = Material.IRON_BOOTS;
                break;
            case "DIAMOND":
                helmetMaterial = Material.DIAMOND_HELMET;
                chestplateMaterial = Material.DIAMOND_CHESTPLATE;
                leggingsMaterial = Material.DIAMOND_LEGGINGS;
                bootsMaterial = Material.DIAMOND_BOOTS;
                break;
            default:
                // Fallback para couro
                helmetMaterial = Material.LEATHER_HELMET;
                chestplateMaterial = Material.LEATHER_CHESTPLATE;
                leggingsMaterial = Material.LEATHER_LEGGINGS;
                bootsMaterial = Material.LEATHER_BOOTS;
                break;
        }
        
        // Equipar armadura completa
        inv.setHelmet(Item.of(helmetMaterial).unbreakable());
        inv.setChestplate(Item.of(chestplateMaterial).unbreakable());
        inv.setLeggings(Item.of(leggingsMaterial).unbreakable());
        inv.setBoots(Item.of(bootsMaterial).unbreakable());

        user.getContext().setArmorItem(item);

        // Aplicar encantamentos de armadura de forma síncrona
        if (team.hasArmor()) {
            Core.getPlatform().runSync(() -> {
                team.getArmor().applyEnchantment(getPlayer());
            });
        }
        
        // Se não tinha armadura, equipar automaticamente
        if (hasNoArmor) {
            getPlayer().sendMessage("§aArmadura completa equipada automaticamente!");
        }

        return true;
    }
    
    /**
     * Adiciona peças individuais de armadura para EggWars
     * Organizadas em colunas: cada tipo de armadura em uma coluna
     * Coluna 1: Malha | Coluna 2: Couro | Coluna 3: Ferro | Coluna 4: Diamante | Coluna 5: Diamante Prot II
     */
    private void addCustomArmorItems() {
        // Coluna 1: Malha (slots 19, 28, 37, 46)
        addArmorPiece(19, Material.CHAINMAIL_HELMET, "Capacete de Malha", Material.IRON_INGOT, 5, false, 0);
        addArmorPiece(28, Material.CHAINMAIL_CHESTPLATE, "Peitoral de Malha", Material.IRON_INGOT, 8, false, 0);
        addArmorPiece(37, Material.CHAINMAIL_LEGGINGS, "Calça de Malha", Material.IRON_INGOT, 5, false, 0);
        addArmorPiece(46, Material.CHAINMAIL_BOOTS, "Bota de Malha", Material.IRON_INGOT, 2, false, 0);
        
        // Coluna 2: Couro (slots 20, 29, 38, 47)
        addArmorPiece(20, Material.LEATHER_HELMET, "Capacete de Couro", Material.IRON_INGOT, 10, false, 0);
        addArmorPiece(29, Material.LEATHER_CHESTPLATE, "Peitoral de Couro", Material.IRON_INGOT, 15, false, 0);
        addArmorPiece(38, Material.LEATHER_LEGGINGS, "Calça de Couro", Material.IRON_INGOT, 10, false, 0);
        addArmorPiece(47, Material.LEATHER_BOOTS, "Bota de Couro", Material.IRON_INGOT, 5, false, 0);
        
        // Coluna 3: Ferro (slots 21, 30, 39, 48)
        addArmorPiece(21, Material.IRON_HELMET, "Capacete de Ferro", Material.GOLD_INGOT, 3, false, 0);
        addArmorPiece(30, Material.IRON_CHESTPLATE, "Peitoral de Ferro", Material.GOLD_INGOT, 5, false, 0);
        addArmorPiece(39, Material.IRON_LEGGINGS, "Calça de Ferro", Material.GOLD_INGOT, 3, false, 0);
        addArmorPiece(48, Material.IRON_BOOTS, "Bota de Ferro", Material.GOLD_INGOT, 1, false, 0);
        
        // Coluna 4: Diamante (slots 22, 31, 40, 49)
        addArmorPiece(22, Material.DIAMOND_HELMET, "Capacete de Diamante", Material.DIAMOND, 2, false, 0);
        addArmorPiece(31, Material.DIAMOND_CHESTPLATE, "Peitoral de Diamante", Material.DIAMOND, 3, false, 0);
        addArmorPiece(40, Material.DIAMOND_LEGGINGS, "Calça de Diamante", Material.DIAMOND, 2, false, 0);
        addArmorPiece(49, Material.DIAMOND_BOOTS, "Bota de Diamante", Material.DIAMOND, 1, false, 0);
        
        // Coluna 5: Diamante com Proteção II (slots 23, 32, 41, 50)
        addArmorPiece(23, Material.DIAMOND_HELMET, "Capacete de Diamante (Prot II)", Material.DIAMOND, 3, true, 2);
        addArmorPiece(32, Material.DIAMOND_CHESTPLATE, "Peitoral de Diamante (Prot II)", Material.DIAMOND, 4, true, 2);
        addArmorPiece(41, Material.DIAMOND_LEGGINGS, "Calça de Diamante (Prot II)", Material.DIAMOND, 3, true, 2);
        addArmorPiece(50, Material.DIAMOND_BOOTS, "Bota de Diamante (Prot II)", Material.DIAMOND, 2, true, 2);
    }
    
    /**
     * Adiciona uma peça individual de armadura na loja
     */
    private void addArmorPiece(int slot, Material armorMaterial, String name, Material costMaterial, int cost, boolean hasProtection, int protectionLevel) {
        PlayerInventory inv = getPlayer().getInventory();
        
        // Determinar qual slot de armadura corresponde
        Material currentArmor = null;
        String slotName = "";
        if (armorMaterial.name().contains("HELMET")) {
            currentArmor = inv.getHelmet() != null ? inv.getHelmet().getType() : null;
            slotName = "capacete";
        } else if (armorMaterial.name().contains("CHESTPLATE")) {
            currentArmor = inv.getChestplate() != null ? inv.getChestplate().getType() : null;
            slotName = "peitoral";
        } else if (armorMaterial.name().contains("LEGGINGS")) {
            currentArmor = inv.getLeggings() != null ? inv.getLeggings().getType() : null;
            slotName = "calça";
        } else if (armorMaterial.name().contains("BOOTS")) {
            currentArmor = inv.getBoots() != null ? inv.getBoots().getType() : null;
            slotName = "bota";
        }
        
        // Verificar se já tem essa peça equipada
        // Se já tem o mesmo material, bloqueia (exceto se for upgrade de diamante)
        boolean alreadyEquipped = false;
        if (currentArmor != null) {
            if (armorMaterial.name().contains("DIAMOND") && hasProtection) {
                // Se está tentando comprar diamante COM proteção, verificar se já tem diamante COM proteção
                ItemStack currentItem = null;
                if (armorMaterial.name().contains("HELMET")) {
                    currentItem = inv.getHelmet();
                } else if (armorMaterial.name().contains("CHESTPLATE")) {
                    currentItem = inv.getChestplate();
                } else if (armorMaterial.name().contains("LEGGINGS")) {
                    currentItem = inv.getLeggings();
                } else if (armorMaterial.name().contains("BOOTS")) {
                    currentItem = inv.getBoots();
                }
                
                // Se já tem diamante e tem proteção II, bloqueia
                if (currentItem != null && currentItem.getType().name().contains("DIAMOND")) {
                    alreadyEquipped = currentItem.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL) 
                                   && currentItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) >= protectionLevel;
                }
            } else {
                // Para outros casos, verificar se é exatamente o mesmo material
                alreadyEquipped = currentArmor.equals(armorMaterial);
            }
        }
        
        // Determinar cor do preço
        String costColor = costMaterial == Material.IRON_INGOT ? "§f" : 
                          costMaterial == Material.GOLD_INGOT ? "§e" : "§b";
        String costName = costMaterial == Material.IRON_INGOT ? "Ferro" : 
                         costMaterial == Material.GOLD_INGOT ? "Ouro" : "Diamante";
        
        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Preço: " + costColor + cost + "x " + costName,
                ""
        ));
        
        if (hasProtection) {
            lore.add("§7Proteção: §a" + protectionLevel);
            lore.add("");
        }
        
        if (alreadyEquipped) {
            lore.add("§cVocê já possui esta peça equipada!");
            lore.add("§cNão é possível comprar novamente.");
        } else {
            lore.add("§7Será equipada automaticamente");
            lore.add("§7ao comprar.");
            lore.add("");
            lore.add("§eClique para comprar!");
        }
        
        Item item = Item.of(armorMaterial).name(alreadyEquipped ? "§c" + name : "§a" + name).lore(lore);
        
        if (hasProtection) {
            item.enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
        }
        
        item.click(event -> {
            // Re-verificar se já tem equipado (pode ter mudado desde que o menu foi aberto)
            Material currentCheck = null;
            if (armorMaterial.name().contains("HELMET")) {
                currentCheck = inv.getHelmet() != null ? inv.getHelmet().getType() : null;
            } else if (armorMaterial.name().contains("CHESTPLATE")) {
                currentCheck = inv.getChestplate() != null ? inv.getChestplate().getType() : null;
            } else if (armorMaterial.name().contains("LEGGINGS")) {
                currentCheck = inv.getLeggings() != null ? inv.getLeggings().getType() : null;
            } else if (armorMaterial.name().contains("BOOTS")) {
                currentCheck = inv.getBoots() != null ? inv.getBoots().getType() : null;
            }
            
            boolean currentlyEquipped = false;
            if (currentCheck != null) {
                if (armorMaterial.name().contains("DIAMOND") && hasProtection) {
                    ItemStack currentItem = null;
                    if (armorMaterial.name().contains("HELMET")) {
                        currentItem = inv.getHelmet();
                    } else if (armorMaterial.name().contains("CHESTPLATE")) {
                        currentItem = inv.getChestplate();
                    } else if (armorMaterial.name().contains("LEGGINGS")) {
                        currentItem = inv.getLeggings();
                    } else if (armorMaterial.name().contains("BOOTS")) {
                        currentItem = inv.getBoots();
                    }
                    if (currentItem != null && currentItem.getType().name().contains("DIAMOND")) {
                        currentlyEquipped = currentItem.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL) 
                                         && currentItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) >= protectionLevel;
                    }
                } else {
                    currentlyEquipped = currentCheck.equals(armorMaterial);
                }
            }
            
            if (currentlyEquipped) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê já possui esta peça de armadura equipada!");
                return;
            }
            
            if (BukkitUtil.getItemAmount(getPlayer(), costMaterial) < cost) {
                sound(MenuSound.ERROR);
                getPlayer().sendMessage("§cVocê não possui " + costName + " suficientes. Precisa de " + cost + "x " + costName + ".");
                return;
            }
            
            // Criar item de armadura
            Item armorItem = Item.of(armorMaterial).unbreakable();
            if (hasProtection) {
                armorItem.enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
            }
            
            // Equipar automaticamente
            if (armorMaterial.name().contains("HELMET")) {
                inv.setHelmet(armorItem);
            } else if (armorMaterial.name().contains("CHESTPLATE")) {
                inv.setChestplate(armorItem);
            } else if (armorMaterial.name().contains("LEGGINGS")) {
                inv.setLeggings(armorItem);
            } else if (armorMaterial.name().contains("BOOTS")) {
                inv.setBoots(armorItem);
            }
            
            BukkitUtil.removeItemByAmount(getPlayer(), costMaterial, cost);
            sound(MenuSound.DONE);
            getPlayer().sendMessage("§a" + name + " equipada automaticamente!");
            
            // Aplicar encantamentos de armadura do time se houver
            if (team.hasArmor()) {
                Core.getPlatform().runSync(() -> {
                    team.getArmor().applyEnchantment(getPlayer());
                });
            }
            
            handle();
        });
        
        addItem(slot, item);
    }

    protected void handleToolPurchase(EggWarsItem item) {
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
}
