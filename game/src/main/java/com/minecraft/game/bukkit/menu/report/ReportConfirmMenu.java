package com.minecraft.game.bukkit.menu.report;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.report.ReportContext;
import com.minecraft.game.report.ReportReason;
import com.minecraft.game.report.ReportService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Passo 3: detalhes opcionais e confirmação. */
public final class ReportConfirmMenu extends Menu {

    private static final String[][] DETAIL_PRESETS = {
            {"Sem detalhes", ""},
            {"Hack / trapaça", "Suspeita de uso de hack ou trapaça."},
            {"Chat tóxico", "Ofensas, spam ou toxicidade no chat."},
            {"Assédio", "Assédio, bullying ou perseguição."},
            {"Abuso de bug", "Explorando bug ou glitch do servidor."}
    };

    private static final int[] PRESET_SLOTS = {29, 30, 31, 32, 33};

    private final Account account;

    public ReportConfirmMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lDenúncia §7| §fConfirmar", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        ReportContext ctx = ReportContext.of(player.getUniqueId());
        if (ctx == null || !ctx.isComplete()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§cSessão expirada")
                    .lore("§7Use §e/report §7novamente")
                    .click(p -> p.closeInventory()));
            closeButton(40);
            return;
        }

        String target = ctx.targetName();
        ReportReason reason = ctx.reason();
        String details = ctx.details();

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target)
                .name("§fResumo da denúncia")
                .lore(
                        "§7Alvo: §f" + target,
                        "§7Motivo: §e" + reason.label(),
                        "§7Detalhes: §f" + (details.isBlank() ? "—" : details),
                        "",
                        "§8Opcional: clique em um detalhe rápido"
                ));

        for (int i = 0; i < DETAIL_PRESETS.length && i < PRESET_SLOTS.length; i++) {
            String label = DETAIL_PRESETS[i][0];
            String text = DETAIL_PRESETS[i][1];
            boolean selected = text.equals(details) || (text.isEmpty() && details.isEmpty());
            MenuItem item = MenuItem.of(Material.WRITABLE_BOOK)
                    .name((selected ? "§a✔ " : "§7") + label)
                    .lore("§7" + (text.isBlank() ? "Enviar sem descrição extra" : text), "", "§eClique para selecionar")
                    .click(p -> {
                        clickSound();
                        ReportContext session = ReportContext.of(p.getUniqueId());
                        if (session != null) {
                            session.setDetails(text);
                            reopen();
                        }
                    });
            if (selected) item.glow();
            set(PRESET_SLOTS[i], item);
        }

        set(20, MenuItem.of(Material.LIME_WOOL)
                .name("§a§lEnviar denúncia")
                .lore("§7A equipe será notificada", "", "§aClique para confirmar")
                .glow()
                .click(p -> {
                    clickSound();
                    ReportContext session = ReportContext.of(p.getUniqueId());
                    if (session == null || !session.isComplete()) {
                        p.closeInventory();
                        return;
                    }
                    ReportService.submit(account, session.targetName(), session.reason(), session.details());
                    p.closeInventory();
                }));

        set(24, MenuItem.of(Material.RED_WOOL)
                .name("§c§lCancelar")
                .lore("§7Descartar denúncia")
                .click(p -> {
                    clickSound();
                    ReportContext.close(p.getUniqueId());
                    p.closeInventory();
                }));

        back(40);
        closeButton(44);
    }
}
