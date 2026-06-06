package com.minecraft.lobbyhost.bukkit.menu.eventos;

import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.network.EventosAccessGuard;
import com.minecraft.lobbyhost.network.EventosAccessService;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu staff para liberar ou bloquear a entrada no servidor de eventos.
 */
public final class EventosAccessMenu extends Menu {

    public EventosAccessMenu(Player player) {
        super(player, ModernText.brandGradient("Eventos — Acesso"), 3, null);
    }

    @Override
    public void build() {
        clear();

        boolean open = EventosAccessGuard.isOpen();

        set(4, MenuItem.of(Material.NETHER_STAR)
                .miniName(ModernText.brandGradient("Status dos eventos"))
                .miniLore(
                        ModernText.mini("Estado atual: " + EventosAccessService.statusColored()),
                        "",
                        ModernText.mini("Alterações sincronizam com o proxy."),
                        ModernText.action("Clique para atualizar")
                )
                .click(p -> new EventosAccessMenu(p).open()));

        set(11, MenuItem.of(open ? Material.LIME_CONCRETE : Material.GREEN_WOOL)
                .miniName(ModernText.mini("<green><bold>Liberar entrada</bold>"))
                .miniLore(
                        ModernText.mini("Permite que jogadores entrem em /evento"),
                        ModernText.mini("e no modo Eventos da bússola."),
                        "",
                        open
                                ? ModernText.mini("<gray>Já está liberado.</gray>")
                                : ModernText.action("Clique para liberar")
                )
                .click(this::unlock));

        set(15, MenuItem.of(open ? Material.RED_WOOL : Material.RED_CONCRETE)
                .miniName(ModernText.mini("<red><bold>Bloquear entrada</bold>"))
                .miniLore(
                        ModernText.mini("Impede conexão ao backend de eventos"),
                        ModernText.mini("até ser liberado novamente."),
                        "",
                        open
                                ? ModernText.action("Clique para bloquear")
                                : ModernText.mini("<gray>Já está bloqueado.</gray>")
                )
                .click(this::lock));

        closeButton(22);
    }

    private void unlock(Player p) {
        if (EventosAccessGuard.isOpen()) {
            p.sendMessage(ModernText.parse(ModernText.mini("<yellow>Eventos já está aberto.</yellow>")));
            reopen();
            return;
        }
        if (!EventosAccessService.publishToggle(true)) {
            p.sendMessage(ModernText.parse(ModernText.mini("<red>Redis indisponível. Use /proxy eventos unlock no Velocity.</red>")));
            return;
        }
        EventosAccessGuard.syncFromNetwork(true);
        p.sendMessage(ModernText.parse(ModernText.mini("<green>Entrada nos eventos liberada na rede.</green>")));
        reopen();
    }

    private void lock(Player p) {
        if (!EventosAccessGuard.isOpen()) {
            p.sendMessage(ModernText.parse(ModernText.mini("<yellow>Eventos já está fechado.</yellow>")));
            reopen();
            return;
        }
        if (!EventosAccessService.publishToggle(false)) {
            p.sendMessage(ModernText.parse(ModernText.mini("<red>Redis indisponível. Use /proxy eventos lock no Velocity.</red>")));
            return;
        }
        EventosAccessGuard.syncFromNetwork(false);
        p.sendMessage(ModernText.parse(ModernText.mini("<red>Entrada nos eventos bloqueada na rede.</red>")));
        reopen();
    }
}
