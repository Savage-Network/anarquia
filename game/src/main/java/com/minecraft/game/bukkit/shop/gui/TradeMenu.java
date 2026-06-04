package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.trade.TradeManager;
import com.minecraft.game.bukkit.shop.trade.TradeSession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Troca segura estilo marketplace — deposita itens e confirma.
 */
public final class TradeMenu extends Menu {

    private final TradeSession session;
    private final boolean sideA;

    public TradeMenu(Player player, TradeSession session, boolean sideA) {
        super(player, "§8§lTroca §7| §f" + otherName(session, player), 4, null);
        this.session = session;
        this.sideA = sideA;
    }

    private static String otherName(TradeSession session, Player player) {
        Player other = session.other(player);
        return other != null ? other.getName() : "?";
    }

    @Override
    public void build() {
        clear();
        for (int i = 0; i < 12; i++) {
            int slot = i < 4 ? i + 1 : i + 5;
            int idx = i;
            ItemStack current = session.offer(sideA ? session.playerA() : session.playerB())[idx];
            set(slot, MenuItem.of(current != null ? current.getType() : Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                    current != null ? current.getAmount() : 1)
                    .name(current != null ? "§f" + current.getType().name() : "§8+ item da mao")
                    .lore("§7Clique com item na mao", "§7para adicionar/remover")
                    .click(p -> toggleItem(p, idx)));
        }

        boolean confirmed = session.isConfirmed(player.getUniqueId());
        set(22, MenuItem.of(confirmed ? Material.LIME_WOOL : Material.RED_WOOL)
                .name(confirmed ? "§aConfirmado" : "§cConfirmar troca")
                .lore("§7Ambos devem confirmar", "", "§eClique para alternar")
                .click(p -> {
                    session.toggleConfirm(p.getUniqueId());
                    tryComplete();
                    open();
                }));

        set(26, MenuItem.of(Material.BARRIER).name("§cCancelar")
                .click(p -> {
                    TradeManager.end(session);
                    p.closeInventory();
                    Player other = session.other(p);
                    if (other != null) {
                        other.closeInventory();
                        other.sendMessage("§cTroca cancelada.");
                    }
                }));
    }

    private void toggleItem(Player p, int idx) {
        ItemStack[] offer = session.offer(p.getUniqueId());
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (offer[idx] != null) {
            var left = p.getInventory().addItem(offer[idx]);
            offer[idx] = null;
        } else if (!hand.getType().isAir()) {
            offer[idx] = hand.clone();
            hand.setAmount(0);
            p.getInventory().setItemInMainHand(null);
        }
        session.toggleConfirm(p.getUniqueId());
        session.toggleConfirm(p.getUniqueId());
        reopen();
    }

    private void tryComplete() {
        if (!session.bothConfirmed()) return;
        Player a = Bukkit.getPlayer(session.playerA());
        Player b = Bukkit.getPlayer(session.playerB());
        if (a == null || b == null) return;

        for (ItemStack stack : session.offer(session.playerB())) {
            if (stack != null) give(a, stack);
        }
        for (ItemStack stack : session.offer(session.playerA())) {
            if (stack != null) give(b, stack);
        }

        a.sendMessage("§aTroca concluida!");
        b.sendMessage("§aTroca concluida!");
        a.closeInventory();
        b.closeInventory();
        TradeManager.end(session);
    }

    private static void give(Player p, ItemStack stack) {
        p.getInventory().addItem(stack.clone());
    }
}
