package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.report.ReportReason;
import com.minecraft.lobbyhost.report.ReportService;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI: escolhe motivo (linha 1) e jogador alvo (linha 2).
 */
public final class ReportMenu extends Menu {

    private static final int[] REASON_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] PLAYER_SLOTS = {20, 21, 22, 23, 24, 29, 30};

    private final Account account;

    public ReportMenu(Player player, Account account) {
        super(player, ModernText.menuTitle("Denunciar jogador"), 4, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        ReportReason selected = ReportService.getPendingReason(player);

        set(4, MenuItem.of(Material.PAPER)
                .miniName(ModernText.gradient("Nova denúncia", ModernText.ERROR, "#FCA5A5"))
                .miniLore(
                        ModernText.mini("1. Escolha o motivo abaixo"),
                        ModernText.mini("2. Clique no jogador denunciado"),
                        "",
                        selected != null
                                ? "<!italic>Motivo: " + selected.displayMini()
                                : ModernText.mini("Nenhum motivo selecionado")
                ));

        ReportReason[] reasons = ReportReason.values();
        for (int i = 0; i < REASON_SLOTS.length && i < reasons.length; i++) {
            ReportReason reason = reasons[i];
            boolean active = reason == selected;
            MenuItem item = MenuItem.of(materialFor(reason))
                    .miniName(reason.displayMini() + (active ? " <#3DDC97>✔" : ""))
                    .miniLore(
                            ModernText.action("Clique para selecionar"),
                            "",
                            ModernText.mini("Depois clique no jogador")
                    )
                    .click(p -> {
                        ReportService.setPendingReason(p, reason);
                        reopen();
                    });
            if (active) item.glow();
            set(REASON_SLOTS[i], item);
        }

        List<Player> targets = new ArrayList<>();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            targets.add(other);
        }

        int slotIdx = 0;
        for (Player target : targets) {
            if (slotIdx >= PLAYER_SLOTS.length) break;
            String nick = target.getName();
            set(PLAYER_SLOTS[slotIdx++], MenuItem.of(Material.PLAYER_HEAD)
                    .skullOwner(nick)
                    .miniName(ModernText.label(nick))
                    .miniLore(
                            ModernText.mini("Denunciar este jogador"),
                            ModernText.action("Clique para enviar")
                    )
                    .click(p -> {
                        ReportReason reason = ReportService.getPendingReason(p);
                        if (reason == null) {
                            p.sendMessage(ModernText.parse("<!italic><" + ModernText.WARN
                                    + ">Selecione um motivo antes do jogador."));
                            return;
                        }
                        ReportService.submit(account, nick, reason, "");
                        p.closeInventory();
                    }));
        }

        if (targets.isEmpty()) {
            set(PLAYER_SLOTS[0], MenuItem.of(Material.BARRIER)
                    .miniName(ModernText.mini("Ninguém online para denunciar"))
                    .miniLore(ModernText.hint("report <nick> <motivo>")));
        }

        closeButton(31);
    }

    private static Material materialFor(ReportReason reason) {
        return switch (reason) {
            case CHEAT -> Material.IRON_SWORD;
            case CHAT -> Material.WRITABLE_BOOK;
            case HARASS -> Material.BARRIER;
            case BUG -> Material.SPIDER_EYE;
            case OTHER -> Material.PAPER;
        };
    }
}
