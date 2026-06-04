package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.title.CollectibleMusicArtist;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

/**
 * Seletor apenas de {@link CollectibleCategory#TITLE}, incluindo subcategorias
 * ({@link CollectibleTitleGroup}). Outros cosméticos usam {@link CollectibleSelectorMenu}.
 */
public class TitleCollectibleSelectorMenu extends Menu {

    private final Account account;
    private final List<Collectible> list;

    private SortMode sortMode = SortMode.NONE;
    private boolean ownFirst = true;

    /** null = todos os títulos */
    private CollectibleTitleGroup titleSubgroupFilter;

    /** null com Músicas = todos os artistas (filtro desligado). */
    private CollectibleMusicArtist musicArtistFilter;

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

    public TitleCollectibleSelectorMenu(Player player, Menu last) {
        super(player, CollectibleCategory.TITLE.getName(), last, 6, 21);
        this.account = Core.getAccountController().of(player.getUniqueId());
        this.list = Core.getCollectibleController().list(CollectibleCategory.TITLE);
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
            if (titleSubgroupFilter != null) {
                List<String> barrierLore = new ArrayList<>(Arrays.asList(
                        "§7Não há títulos nesta subcategoria.",
                        "§7Altere o Livro §8&§7 Pena ou o §6papel §7de artista ao lado",
                        "§7na barra inferior.",
                        "",
                        "§eAlterne até §aTodos §epara ver tudo."));
                if (titleSubgroupFilter == CollectibleTitleGroup.MUSICAS && musicArtistFilter != null) {
                    barrierLore.addAll(Arrays.asList(
                            "",
                            "§7Filtrando artista: §f" + describeMusicArtistFilter()));
                }
                addItem(22, Item.of(Material.BARRIER, "§cNenhum título neste tipo", barrierLore.toArray(new String[0])));
            } else {
                addItem(22, Item.of(Material.BARRIER, "§cNenhum título disponível",
                        "",
                        "§7Não há títulos carregados",
                        "§7neste servidor no momento."));
            }
        } else {
            buildPageItems(toDisplay, 10, (collectible, slot) -> {
                boolean using = account.isUsingCollectible(collectible);
                boolean available = collectible.hasAccess(account.getRank().getType());

                List<String> lore = new ArrayList<>(Arrays.asList(
                        "§8" + CollectibleCategory.TITLE.getName(),
                        ""
                ));
                if (collectible instanceof TitleCollectible) {
                    TitleCollectible tt = (TitleCollectible) collectible;
                    lore.add("§fTipo: " + tt.getTitleGroup().getColoredLabel());
                    if (tt.getTitleGroup() == CollectibleTitleGroup.MUSICAS) {
                        lore.add("§fArtista: " + tt.getMusicArtist().getColoredLabel());
                    }
                    lore.add("");
                }
                if (!collectible.getLore().isEmpty()) {
                    lore.addAll(collectible.getLore());
                    lore.add("");
                }

                lore.add("§7Raridade: " + collectible.getRarity().getColoredName());
                lore.add("");
                lore.add(using ? "§cJá selecionada." : available ? "§eClique para selecionar!" : "§cIndisponível.");

                Item icon = collectible.getIcon();

                Item item = (available ? icon : Item.of(Material.INK_SACK, 8))
                        .name((using ? "§e" : !available ? "§c" : "§a") + collectible.getName())
                        .lore(lore);

                if (item.getType().equals(Material.SKULL_ITEM))
                    item.skullByBase64(collectible.getHeadValue());

                addItem(slot, item.click(event -> {
                    if (!available) {
                        sound(MenuSound.ERROR);
                        account.send("§cVocê não possui " + collectible.getName() + ".");
                        return;
                    }

                    if (using) {
                        sound(MenuSound.ERROR);
                        account.send("§cO " + collectible.getName() + " já foi selecionado.");
                        return;
                    }

                    close();
                    sound(MenuSound.SUCCESS);

                    CollectibleOperator.unloadConflicts(getPlayer(), CollectibleCategory.TITLE);

                    CollectibleOperator operator = CollectibleOperator.of(getPlayer(), CollectibleCategory.TITLE);

                    if (operator != null) {
                        operator.cancel();
                        operator.handle(collectible);
                    } else
                        CollectibleOperator.start(getPlayer(), collectible);

                    account.send("§eO §6" + collectible.getName() + "§e foi selecionado.");
                }));
            });
        }

        CollectibleOperator operator = CollectibleOperator.of(getPlayer(), CollectibleCategory.TITLE);
        if (operator != null) {
            addItem(getTotalSlots() - (getTotalPages() > 1 ? 5 : 6), Item.of(Material.BARRIER, "§cRemover")
                    .click(event -> {
                        close();
                        sound(MenuSound.PAGINATED);
                        account.send("§eO §6" + operator.getCollectible().getName() + "§e foi removido.");
                        CollectibleOperator.unload(getPlayer(), CollectibleCategory.TITLE);
                    }));
        }

        String nextLabel = titleSubgroupPeekNextDisplay();
        addItem(45,
                Item.of(Material.BOOK_AND_QUILL)
                        .name("§aTipo de título: " + describeTitleSubgroupFilterDisplay())
                        .lore(Arrays.asList(
                                "§7Filtra apenas títulos; não altera outros cosméticos.",
                                "",
                                "§fAtualmente: " + describeTitleSubgroupFilterDisplay(),
                                "§fPróximo: §e" + nextLabel,
                                "",
                                titleSubgroupFilter == CollectibleTitleGroup.MUSICAS
                                        ? "§7Em §5Músicas§7, use o §6papel §7de artista §8(com brilho)§7 ao lado."
                                        : "§7Em §5Músicas§7, aparece o §6papel§7 de filtro §fpor artista§7.",
                                "",
                                "§eClique para alternar."
                        ))
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            titleSubgroupFilter = titleSubgroupAdvance(titleSubgroupFilter);
                            if (titleSubgroupFilter != CollectibleTitleGroup.MUSICAS)
                                musicArtistFilter = null;
                            setPageNumber(1);
                            handle();
                        }));

        CollectibleMusicArtist nextArtistPeek = peekNextMusicArtist(musicArtistFilter);
        boolean onMusicSubgroup = titleSubgroupFilter == CollectibleTitleGroup.MUSICAS;
        if (onMusicSubgroup) {
            /* Igual aos títulos Músicas (paper); brilho só para não se perder no inventário. */
            Item disc = Item.of(Material.PAPER, "§6§l♪ §aArtista §8· " + describeMusicArtistFilter())
                    .lore(Arrays.asList(
                            "§7Mesmo formato dos títulos §8(papel)§7 — filtro rápido.",
                            "",
                            "§fAtual: " + describeMusicArtistFilter(),
                            "§fPróximo: " + formatMusicArtistLabel(nextArtistPeek),
                            "",
                            "§eClique para alternar artista."));
            disc.unsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            disc.flags(ItemFlag.HIDE_ENCHANTS);
            addItem(46,
                    disc.click(event -> {
                        sound(MenuSound.PAGINATED);
                        musicArtistFilter = nextArtistPeek;
                        setPageNumber(1);
                        handle();
                    }));
        } else {
            addItem(46,
                    Item.of(Material.STONE_BUTTON, "§8Artista §7(indisponível)")
                            .lore(Arrays.asList(
                                    "§7Primeiro escolha §5Músicas §7no",
                                    "§7livro §8(& §7pena§8)§7 ao lado §8(§7slot 45§8)§7.",
                                    "",
                                    "§7Esse papel filtra §finterprete / MC §7nas músicas."))
                            .click(event -> sound(MenuSound.ERROR)));
        }

        addItem(getTotalSlots() - (getTotalPages() > 1 ? 7 : 8),
                Item.of(Material.HOPPER)
                        .name("§aOrdenado por: " + sortMode.getDisplay())
                        .lore("§fAtualmente ordenado por " + sortMode.getDisplay() + ".")
                        .lore("", "§fPróximo filtro: §e" + sortMode.next().getDisplay(), "§eClique esquerdo para alterar.")
                        .lore("", "§fSeus títulos primeiro: " + (ownFirst ? "§aSim" : "§cNão"), "§eClique direito para alterar.")
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

        if (titleSubgroupFilter != null) {
            copy.removeIf(c -> !(c instanceof TitleCollectible)
                    || ((TitleCollectible) c).getTitleGroup() != titleSubgroupFilter);
        }

        if (titleSubgroupFilter == CollectibleTitleGroup.MUSICAS && musicArtistFilter != null) {
            copy.removeIf(c -> !(c instanceof TitleCollectible)
                    || ((TitleCollectible) c).getMusicArtist() != musicArtistFilter);
        }

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

    private CollectibleTitleGroup titleSubgroupAdvance(CollectibleTitleGroup current) {
        CollectibleTitleGroup[] vals = CollectibleTitleGroup.values();
        if (current == null) {
            return vals[0];
        }
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == current) {
                int n = i + 1;
                return n < vals.length ? vals[n] : null;
            }
        }
        return null;
    }

    private String describeTitleSubgroupFilterDisplay() {
        return titleSubgroupFilter == null ? "§aTodos" : titleSubgroupFilter.getColoredLabel();
    }

    private String titleSubgroupPeekNextDisplay() {
        CollectibleTitleGroup next = titleSubgroupAdvance(titleSubgroupFilter);
        return next == null ? "§aTodos" : next.getColoredLabel();
    }

    /** Artistas clicáveis no menu (UNKNOWN não entra — evita filtros vazios). */
    private static CollectibleMusicArtist[] selectableMusicArtists() {
        return Arrays.stream(CollectibleMusicArtist.values())
                .filter(a -> a != CollectibleMusicArtist.UNKNOWN)
                .toArray(CollectibleMusicArtist[]::new);
    }

    /** Próximo no ciclo: §aTodos§r → enums → §aTodos§r */
    private static CollectibleMusicArtist peekNextMusicArtist(CollectibleMusicArtist current) {
        CollectibleMusicArtist[] v = selectableMusicArtists();
        if (v.length == 0) {
            return null;
        }
        if (current == null) {
            return v[0];
        }
        for (int i = 0; i < v.length; i++) {
            if (v[i] == current) {
                int n = i + 1;
                return n < v.length ? v[n] : null;
            }
        }
        /* current era UNKNOWN ou artista antigo removido: volta ao ciclo válido */
        return v[0];
    }

    private String describeMusicArtistFilter() {
        return formatMusicArtistLabel(musicArtistFilter);
    }

    private static String formatMusicArtistLabel(CollectibleMusicArtist artist) {
        return artist == null ? "§aTodos os artistas" : artist.getColoredLabel();
    }
}
