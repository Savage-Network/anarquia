package com.minecraft.lobbyhost.bukkit.menu.report;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.report.ReportContext;
import com.minecraft.lobbyhost.report.ReportReason;
import com.minecraft.lobbyhost.report.ReportService;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
        super(player, ModernText.menuTitle("Denúncia — Confirmar"), 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        ReportContext ctx = ReportContext.of(player.getUniqueId());
        if (ctx == null || !ctx.isComplete()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .miniName("<!italic><" + ModernText.ERROR + ">Sessão expirada")
                    .miniLore(ModernText.hint("/report")));
            closeButton(40);
            return;
        }

        String target = ctx.targetName();
        ReportReason reason = ctx.reason();
        String details = ctx.details();

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target)
                .miniName(ModernText.gradient("Resumo", ModernText.ERROR, "#FCA5A5"))
                .miniLore(
                        ModernText.mini("Alvo: " + target),
                        ModernText.mini("Motivo: " + reason.label()),
                        ModernText.mini("Detalhes: " + (details.isBlank() ? "—" : details)),
                        "",
                        ModernText.hint("Detalhe rápido (opcional)")
                ));

        for (int i = 0; i < DETAIL_PRESETS.length && i < PRESET_SLOTS.length; i++) {
            String label = DETAIL_PRESETS[i][0];
            String text = DETAIL_PRESETS[i][1];
            boolean selected = text.equals(details) || (text.isEmpty() && details.isEmpty());
            MenuItem item = MenuItem.of(Material.WRITABLE_BOOK)
                    .miniName((selected ? "<#3DDC97>✔ " : "") + label)
                    .miniLore(ModernText.action("Selecionar"))
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
                .miniName(ModernText.gradient("Enviar", ModernText.SUCCESS, "#86EFAC"))
                .miniLore(ModernText.action("Confirmar denúncia"))
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
                .miniName("<!italic><" + ModernText.ERROR + ">Cancelar")
                .miniLore(ModernText.mini("Descartar denúncia"))
                .click(p -> {
                    clickSound();
                    ReportContext.close(p.getUniqueId());
                    p.closeInventory();
                }));

        back(40);
        closeButton(44);
    }
}
