package com.minecraft.lobbyhost.bukkit.menu.clan;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.Clan;
import com.minecraft.lobbyhost.api.clan.ClanColor;
import com.minecraft.lobbyhost.api.clan.ClanMember;
import com.minecraft.lobbyhost.api.clan.ClanRole;
import com.minecraft.lobbyhost.api.clan.ClanSync;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.util.ModernText;
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
        super(player, ModernText.menuTitle("Clã · Estilos de Tag"), 6, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();

        ClanMember member = clan.getMember(account.getId());
        if (member == null || !member.hasRole(ClanRole.RECRUITER)) {
            set(22, MenuItem.of(Material.BARRIER)
                    .miniName("<!italic><" + ModernText.ERROR + "><bold>Sem permissão</bold>")
                    .miniLore(
                            ModernText.mini("Apenas Recrutador ou Líder"),
                            ModernText.mini("podem alterar o estilo.")
                    ));
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
                .miniName(ModernText.gradient("Preview ao vivo", ModernText.WARN, ModernText.BRAND_B))
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
                .miniName(ModernText.gradient(clan.getName(), ModernText.BRAND_A, ModernText.BRAND_B))
                .lore(
                        "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                        "§7Estilo: §f" + clan.getStyle().getName(),
                        "",
                        "§7Escolha uma cor ou gradiente"
                ));
    }

    private void buildLabels() {
        set(1, MenuItem.of(Material.WHITE_DYE).miniName(ModernText.label("Sólidas")));
        set(3, MenuItem.of(Material.GOLD_INGOT).miniName(ModernText.gradient("Premium", "#FBBF24", "#F59E0B")));
        set(5, MenuItem.of(Material.AMETHYST_SHARD)
                .miniName(ModernText.gradient("Gradientes", "#C084FC", "#F472B6")));
        set(7, MenuItem.of(Material.NETHER_STAR)
                .miniName(ModernText.gradient("Especiais", "#38BDF8", "#818CF8")));
    }

    private void fillPane() {
        MenuItem pane = MenuItem.of(Material.GRAY_STAINED_GLASS_PANE).miniName("<black>");
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
