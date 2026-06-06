package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanColor;
import com.minecraft.game.api.clan.ClanMember;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanSync;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ClanColorMenu extends Menu {

    private static final int[] SOLID_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20};
    private static final int[] PREMIUM_SLOTS = {21, 22};
    private static final int[] GRADIENT_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41};
    private static final int[] SPECIAL_SLOTS = {42, 43};

    private final Account account;
    private final Clan clan;

    public ClanColorMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, "§8§lClã §7| §fEstilos de Tag", 6, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();

        ClanMember member = clan.getMember(account.getId());
        if (member == null || !member.hasRole(ClanRole.RECRUITER)) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§cSem permissão")
                    .lore("§7Apenas Recrutador ou Líder", "§7podem alterar o estilo."));
            back(45);
            return;
        }

        fillPane();
        buildHeader();
        buildLabels();

        String tagSample = clan.getTagWithoutColors();
        ClanColor active = clan.getStyle();

        placeCategory(ClanColor.StyleCategory.SOLID, SOLID_SLOTS, tagSample, active);
        placeCategory(ClanColor.StyleCategory.PREMIUM, PREMIUM_SLOTS, tagSample, active);
        placeCategory(ClanColor.StyleCategory.GRADIENT, GRADIENT_SLOTS, tagSample, active);
        placeCategory(ClanColor.StyleCategory.SPECIAL, SPECIAL_SLOTS, tagSample, active);

        set(49, MenuItem.of(Material.PAINTING)
                .name("§e§lPreview ao vivo")
                .lore(
                        "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                        "",
                        "§7Chat, tab e nametag usam",
                        "§7o mesmo renderizador unificado"
                ));

        back(45);
        closeButton(53);
    }

    private void placeCategory(ClanColor.StyleCategory category, int[] slots,
                               String tagSample, ClanColor active) {
        List<ClanColor> colors = ClanColor.byCategory(category);
        for (int i = 0; i < colors.size() && i < slots.length; i++) {
            set(slots[i], buildColorItem(colors.get(i), tagSample, active));
        }
    }

    private void buildHeader() {
        set(4, MenuItem.of(Material.NAME_TAG).glow()
                .name("§6§l" + clan.getName())
                .lore(
                        "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                        "§7Estilo: §f" + clan.getStyle().getName(),
                        "",
                        "§7Escolha uma cor ou gradiente"
                ));
    }

    private void buildLabels() {
        set(1, MenuItem.of(Material.WHITE_DYE).name("§f§lSólidas"));
        set(3, MenuItem.of(Material.GOLD_INGOT).name("§6§lPremium"));
        set(5, MenuItem.of(Material.AMETHYST_SHARD).name("§d§lGradientes"));
        set(7, MenuItem.of(Material.NETHER_STAR).name("§b§lEspeciais"));
    }

    private void fillPane() {
        MenuItem pane = MenuItem.of(Material.GRAY_STAINED_GLASS_PANE).name(" ");
        for (int slot = 0; slot < 54; slot++) {
            if (itemAt(slot) == null && slot != 45 && slot != 53) {
                set(slot, pane);
            }
        }
    }

    private MenuItem buildColorItem(ClanColor color, String tagSample, ClanColor active) {
        boolean access = color.isFree()
                || (color.getPermission() != null && account.hasPermission(color.getPermission()))
                || account.isStaffer();
        boolean selected = color == active;
        String preview = ClanTagFormatter.formatPreview(color, tagSample);

        String typeLabel = switch (color.getRenderMode()) {
            case HEX -> "§dGradiente hex";
            case YIN_YANG, ALTERNATE -> "§bEfeito especial";
            default -> "§fCor sólida";
        };

        List<String> loreLines = new ArrayList<>();
        loreLines.add("§8▪ §7Nome: §f" + color.getName());
        loreLines.add("§8▪ §7Tipo: " + typeLabel);
        if (color.isPremium()) loreLines.add("§8▪ §6Premium");
        loreLines.add("");
        if (selected) loreLines.add("§a✔ Estilo ativo");
        loreLines.add(access ? "§eClique para aplicar" : "§cBloqueado");
        if (color.getPermission() != null && !access) {
            loreLines.add("§7Requer: §f" + color.getPermission());
        }

        MenuItem item = MenuItem.of(access ? color.getGlass() : Material.GRAY_STAINED_GLASS_PANE)
                .name(preview)
                .lore(loreLines.toArray(String[]::new));

        if (selected) item.glow();

        return item.click(p -> {
            if (!access) {
                clickSound();
                account.send("§cVocê não tem acesso a este estilo.");
                return;
            }
            clan.applyStyle(color);
            ClanSync.refreshMembers(clan);
            successSound();
            account.send("§aEstilo alterado para " + preview + "§a.");
            reopen();
        });
    }
}
