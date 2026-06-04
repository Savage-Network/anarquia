package com.minecraft.eventos.bukkit.manager;

import com.minecraft.eventos.Constant;
import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.account.context.objects.medal.Medal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;

/**
 * Espelho de {@code com.minecraft.core.bukkit.manager.MedalManager} do Savage.
 *
 * <p>Lista interativa de medalhas - exibida no chat por {@code /medal} sem
 * argumentos ou pelo {@code AccountMenu} > Medalhas.
 */
public final class MedalManager {

    private MedalManager() {}

    public static void sendMedals(Account account) {
        List<Medal> available = account.getAvailableMedals();

        // Filtra NONE para nao listar "vazio" na linha visual.
        available.removeIf(m -> m == Medal.NONE);

        if (available.isEmpty()) {
            account.send("§cVoce ainda nao possui medalhas. Visite " + Constant.SERVER_STORE);
            return;
        }

        Component msg = Component.text("§aSuas medalhas: ");
        for (int i = 0; i < available.size(); i++) {
            Medal m = available.get(i);
            String hover = "§eClique para selecionar a medalha "
                    + m.getColoredName()
                    + (account.isUsingMedal(m) ? "\n§a✓ Selecionada." : "");
            Component entry = Component.text(m.getColoredSymbol().trim())
                    .hoverEvent(HoverEvent.showText(Component.text(hover)))
                    .clickEvent(ClickEvent.runCommand("/medal " + m.name()));
            msg = msg.append(entry).append(Component.text("§7 "));
        }
        account.send(msg);

        // Opcao de remover medalha ativa
        if (account.getMedal() != Medal.NONE) {
            account.send(Component.text("§7[§cRemover medalha§7]")
                    .hoverEvent(HoverEvent.showText(Component.text("§eDesequipa a medalha ativa.")))
                    .clickEvent(ClickEvent.runCommand("/medal " + Medal.NONE.name())));
        }
    }
}
