package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class CollectibleSelectorMenu extends Menu {

    private final Account account;
    private final CollectibleCategory category;

    private final List<Collectible> list;

    private SortMode sortMode = SortMode.NONE;
    private boolean ownFirst = true;

    private enum SortMode {
        NONE("Nenhum Filtro"),
        AZ("A-Z"),
        ZA("Z-A"),
        RARITY_ASC("Raridade (↑)"),
        RARITY_DESC("Raridade (↓)");

        private final String display;

        SortMode(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        public SortMode next() {
            switch (this) {
                case NONE:
                    return AZ;
                case AZ:
                    return ZA;
                case ZA:
                    return RARITY_ASC;
                case RARITY_ASC:
                    return RARITY_DESC;
                default:
                    return NONE;
            }
        }
    }

    public CollectibleSelectorMenu(Player player, CollectibleCategory category, Menu last) {
        super(player, category.getName(), last, 6, 21);

        this.account = Core.getAccountController().of(player.getUniqueId());

        this.category = category;
        this.list = Core.getCollectibleController().list(category);
    }

    public CollectibleSelectorMenu(Player player, Menu last) {
        this(player, CollectibleCategory.HAT, last);
    }

    @Override
    public void handle() {
        clear();

        List<Collectible> toDisplay = getSortedList();

        if (toDisplay.isEmpty()) {
            setTotalPages(1);
            if (getPageNumber() > 1) {
                setPageNumber(1);
            }
            addItem(22, Item.of(Material.BARRIER, "§cNenhum cosmético disponível",
                    "",
                    "§7Não há cosméticos desta categoria",
                    "§7disponíveis no momento."));
        } else {
            buildPageItems(toDisplay, 10, (collectible, slot) -> {
                boolean using;
                if (category == CollectibleCategory.JOIN_MESSAGE) {
                    // Para join messages, verificar se está selecionada diretamente
                    com.minecraft.core.api.joinmessage.JoinMessage selectedMsg = account.getSelectedJoinMessageEnum();
                    if (selectedMsg != null && collectible instanceof com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible) {
                        com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible joinMsgCollectible = 
                                (com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible) collectible;
                        using = selectedMsg.equals(joinMsgCollectible.getJoinMessage());
                    } else {
                        using = false;
                    }
                } else {
                    using = account.isUsingCollectible(collectible);
                }
                boolean available = collectible.hasAccess(account.getRank().getType());

                List<String> lore = new ArrayList<>(Arrays.asList(
                        "§8" + collectible.getCategory().getName(),
                        ""
                ));
                if (!collectible.getLore().isEmpty()) {
                    lore.addAll(collectible.getLore());
                    lore.add("");
                }

                lore.add("§7Raridade: " + collectible.getRarity().getColoredName());
                
                // Lógica especial para mensagens de entrada - mostrar prévia
                if (category == CollectibleCategory.JOIN_MESSAGE) {
                    lore.add("");
                    lore.add("§7Prévia:");
                    if (collectible instanceof com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible) {
                        com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible joinMsgCollectible = 
                                (com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible) collectible;
                        lore.add(account.getTag().getByPrefix(account.getTagPrefix()) + getPlayer().getName() + " §6" + joinMsgCollectible.getJoinMessage().getMessage());
                    }
                    lore.add("");
                }
                
                // Lógica especial para partículas
                if (category == CollectibleCategory.PARTICLE) {
                    CollectibleOperator currentOperator = CollectibleOperator.of(getPlayer(), category);
                    boolean hasOtherParticleSelected = currentOperator != null && 
                            !currentOperator.getCollectible().getIdentifier().equals(collectible.getIdentifier());
                    
                    if (hasOtherParticleSelected) {
                        lore.add("");
                        lore.add("§cVocê já tem uma partícula selecionada!");
                        lore.add("§7Remova a partícula atual para selecionar outra.");
                    } else if (using) {
                        lore.add("");
                        lore.add("§eClique para alterar o tipo desta partícula!");
                    } else {
                        lore.add("");
                        lore.add(available ? "§eClique para selecionar!" : "§cIndisponível.");
                    }
                } else if (category == CollectibleCategory.JOIN_MESSAGE) {
                    lore.add("");
                    if (using) {
                        lore.add("§eSelecionada.");
                        lore.add("§7Clique para deselecionar.");
                    } else {
                        lore.add(available ? "§eClique para selecionar!" : "§cIndisponível.");
                    }
                } else {
                    lore.add("");
                    if (using) {
                        lore.add("§cJá selecionada.");
                    } else {
                        lore.add(available ? "§eClique para selecionar!" : "§cIndisponível.");
                    }
                }

                Item icon = collectible.getCategory().equals(CollectibleCategory.BANNER) ? ((BannerCollectible) collectible).component()
                        : collectible.getIcon();

                Item item = (available ? icon : Item.of(Material.INK_SACK, 8))
                        .name((using ? "§e" : !available ? "§c" : "§a") + collectible.getName())
                        .lore(lore);

                if (item.getType().equals(Material.SKULL_ITEM))
                    if (category == CollectibleCategory.EMOTION)
                        item.skullByUrl(collectible.getHeadValue());
                    else
                        item.skullByBase64(collectible.getHeadValue());

                addItem(slot, item.click(event -> {
                    if (!available) {
                        sound(MenuSound.ERROR);
                        account.send("§cVocê não possui " + collectible.getName() + ".");
                        return;
                    }

                    // Lógica especial para partículas
                    if (category == CollectibleCategory.PARTICLE) {
                        CollectibleOperator currentOperator = CollectibleOperator.of(getPlayer(), category);
                        boolean hasOtherParticleSelected = currentOperator != null && 
                                !currentOperator.getCollectible().getIdentifier().equals(collectible.getIdentifier());
                        
                        if (hasOtherParticleSelected) {
                            sound(MenuSound.ERROR);
                            account.send("§cVocê já tem uma partícula selecionada! Remova a atual para selecionar outra.");
                            return;
                        }
                        
                        // Se for a mesma partícula ou nenhuma selecionada, permitir selecionar
                        sound(MenuSound.PAGINATED);
                        new ParticleTypeSelectorMenu(getPlayer(), collectible, this).handle();
                        return;
                    }

                    // Lógica especial para mensagens de entrada
                    if (category == CollectibleCategory.JOIN_MESSAGE) {
                        if (using) {
                            // Desselecionar se já estiver usando
                            account.setSelectedJoinMessage(null);
                            sound(MenuSound.SUCCESS);
                            account.send("§cVocê deselecionou §e" + collectible.getName() + "§c.");
                        } else {
                            // Selecionar nova mensagem
                            com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible joinMsgCollectible = 
                                    (com.minecraft.core.api.collectible.type.joinmessage.JoinMessageCollectible) collectible;
                            account.setSelectedJoinMessage(joinMsgCollectible.getJoinMessage());
                            sound(MenuSound.SUCCESS);
                            account.send("§bVocê selecionou §e" + collectible.getName() + "§b!");
                        }
                        handle();
                        return;
                    }

                    // Para outros cosméticos, não permitir selecionar se já estiver usando
                    if (using) {
                        sound(MenuSound.ERROR);
                        account.send("§cO " + collectible.getName() + " já foi selecionado.");
                        return;
                    }

                    close();
                    sound(MenuSound.SUCCESS);

                    CollectibleOperator.unloadConflicts(getPlayer(), category);

                    CollectibleOperator operator = CollectibleOperator.of(getPlayer(), category);

                    if (operator != null) {
                        operator.cancel();
                        operator.handle(collectible);
                    } else
                        CollectibleOperator.start(getPlayer(), collectible);

                    account.send("§eO §6" + collectible.getName() + "§e foi selecionado.");
                }));
            });
        }

            // Botão de remoção não é necessário para JOIN_MESSAGE
            if (category != CollectibleCategory.JOIN_MESSAGE) {
                CollectibleOperator operator = CollectibleOperator.of(getPlayer(), category);

                if (operator != null) {
                    addItem(getTotalSlots() - (getTotalPages() > 1 ? 5 : 6), Item.of(Material.BARRIER, "§cRemover")
                            .click(event -> {
                                close();
                                sound(MenuSound.PAGINATED);

                                account.send("§eO §6" + operator.getCollectible().getName() + "§e foi removido.");

                                CollectibleOperator.unload(getPlayer(), category);
                            }));
                }
            }

        addItem(getTotalSlots() - (getTotalPages() > 1 ? 7 : 8),
                Item.of(Material.HOPPER)
                        .name("§aOrdenado por: " + sortMode.getDisplay())
                        .lore("§fAtualmente ordenado por " + sortMode.getDisplay() + ".")
                        .lore("", "§fPróximo filtro: §e" + sortMode.next().getDisplay(), "§eClique esquerdo para alterar.")
                        .lore("", "§fSeus cosméticos primeiro: " + (ownFirst ? "§aSim" : "§cNão"), "§eClique direito para alterar.")
                        .click(event -> {
                            switch (event.getClick()) {
                                case LEFT:
                                    sortMode = sortMode.next();
                                    sound(MenuSound.PAGINATED);
                                    handle();
                                    break;
                                case RIGHT:
                                    ownFirst = !ownFirst;
                                    sound(MenuSound.PAGINATED);
                                    handle();
                                    break;
                                default:
                                    break;
                            }
                        }));


        if (isReturnable())
            addBackButton();

        display();
    }

    private List<Collectible> getSortedList() {
        List<Collectible> copy = new ArrayList<>(list);

        Comparator<Collectible> comparator;

        switch (sortMode) {
            case AZ:
                comparator = Comparator.comparing(c -> c.getName().toLowerCase());
                break;
            case ZA:
                comparator = Comparator.comparing((Collectible c) -> c.getName().toLowerCase(), Comparator.reverseOrder());
                break;
            case RARITY_ASC:
                comparator = Comparator.comparingInt(c -> c.getRarity().ordinal());
                break;
            case RARITY_DESC:
                comparator = Comparator.comparingInt((Collectible c) -> c.getRarity().ordinal()).reversed();
                break;
            case NONE:
            default:
                comparator = null;
                break;
        }

        Comparator<Collectible> ownFirstComparator = Comparator.comparing((Collectible c) -> c.hasAccess(account.getRank().getType()) ? 0 : 1);

        if (comparator != null) {
            if (ownFirst) copy.sort(ownFirstComparator.thenComparing(comparator));
            else copy.sort(comparator);
        } else {
            if (ownFirst) copy.sort(ownFirstComparator);
        }

        return copy;
    }
}
