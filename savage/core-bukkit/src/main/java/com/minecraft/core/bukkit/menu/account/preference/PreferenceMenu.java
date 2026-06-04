package com.minecraft.core.bukkit.menu.account.preference;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.manager.list.TagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PreferenceMenu extends Menu {

    private final Account account;
    private final boolean isStaffMenu;

    public PreferenceMenu(Player player, Menu last) {
        this(player, last, false);
    }

    private PreferenceMenu(Player player, Menu last, boolean isStaffMenu) {
        super(player, isStaffMenu ? "Preferências de Staff" : "Preferências", last, 5);

        this.account = Core.getAccountController().of(player.getUniqueId());
        this.isStaffMenu = isStaffMenu;
    }

    public static PreferenceMenu createStaffMenu(Player player, Menu last) {
        return new PreferenceMenu(player, last, true);
    }

    @Override
    public void handle() {
        clear();

        if (isStaffMenu) {
            handleStaffPreferences();
        } else {
            handlePreferences();

            if (account.isStaffer()) {
                addItem(getTotalSlots() - 4, Item.of(Material.NETHER_STAR,
                                "§aPreferências de Staff",
                                "",
                                "§7Clique para ver as",
                                "§7preferências exclusivas",
                                "§7de staff.",
                                "",
                                "§eClique para abrir!")
                        .click(event -> {
                            PreferenceMenu staffMenu = createStaffMenu(getPlayer(), this);
                            staffMenu.handle();
                            sound(MenuSound.SUCCESS);
                        }));
            }
        }

        if (isReturnable()) addBackButton();

        display();
    }

    protected void handlePreferences() {
        Toggle toggle = account.getToggle();

        addItem(10, Item.of(Material.BOOK, getColorByToggle(toggle.isAllowMessages()) + "Mensagens privadas",
                        "§7Permita que os jogadores enviem",
                        "§7mensagens no seu /tell.",
                        "",
                        "§7Atual: " + (toggle.isAllowMessages() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alterar!")
                .click(event -> {
                    toggle.setAllowMessages(!toggle.isAllowMessages());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(11, Item.of(Material.COOKIE, getColorByToggle(toggle.isAllowFriendRequests()) + "Pedidos de amizade",
                        "§7Receba pedidos de amizade",
                        "§7de outros jogadores.",
                        "",
                        "§7Atual: " + (toggle.isAllowFriendRequests() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAllowFriendRequests(!toggle.isAllowFriendRequests());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(12, Item.of(Material.RED_ROSE, getColorByToggle(toggle.isAllowPartyInvite()) + "Pedidos de party",
                        "§7Receba convites de party",
                        "§7e grupos com jogadores.",
                        "",
                        "§7Atual: " + (toggle.isAllowPartyInvite() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAllowPartyInvite(!toggle.isAllowPartyInvite());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(13, Item.of(Material.DIAMOND_SWORD, getColorByToggle(toggle.isAllowClanInvite()) + "Pedidos de clan",
                        "§7Receba convites de clan",
                        "§7e faça parte deles.",
                        "",
                        "§7Atual: " + (toggle.isAllowClanInvite() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAllowClanInvite(!toggle.isAllowClanInvite());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

            addItem(14, Item.of(Material.SIGN, getColorByToggle(toggle.isAllowJoinMessage()) + "Mensagem de entrada",
                            "§7Quando você entrar, os",
                            "§7jogadores serão notificados.",
                            "",
                            "§7Atual: " + (toggle.isAllowJoinMessage() ? "§aAtivado" : "§cDesativado"),
                            "",
                            account.isVIP() ? "§eClique para alternar!" : "§cSem permissão.")
                    .click(event -> {
                        toggle.setAllowJoinMessage(!toggle.isAllowJoinMessage());
                        account.setToggle(toggle);
                        sound(MenuSound.SUCCESS);
                        handle();
                    }));

        addItem(15, Item.of(Material.PAPER, getColorByToggle(toggle.isAllowStatsView()) + "Stats público",
                        "§7Permita que os jogadores vejam",
                        "§7as suas estatísticas.",
                        "",
                        "§7Atual: " + (toggle.isAllowStatsView() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAllowStatsView(!toggle.isAllowStatsView());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(16, Item.of(Material.BED, getColorByToggle(toggle.isConfirmToHub()) + "Proteção de /lobby",
                        "§7Ao tentar voltar ao lobby,",
                        "§7confirme o seu retorno.",
                        "",
                        "§7Atual: " + (toggle.isConfirmToHub() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setConfirmToHub(!toggle.isConfirmToHub());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(19, Item.of(Material.COMPASS, getColorByToggle(toggle.isAllowStatsView()) + "Notificações",
                        "§7Visualize notificações e",
                        "§7mensagens gerais do servidor.",
                        "",
                        "§7Atual: " + (toggle.isAllowStatsView() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAllowStatsView(!toggle.isAllowStatsView());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        if (account.hasClan()) {
            addItem(19, Item.of(Material.NAME_TAG, getColorByToggle(toggle.isAllowClanTag()) + "Clantag",
                            "§7Ative ou desative a sua",
                            "§7clan tag no nickname.",
                            "",
                            "§7Atual: " + (toggle.isAllowClanTag() ? "§aAtivado" : "§cDesativado"),
                            "",
                            "§eClique para alternar!")
                    .click(event -> {
                        toggle.setAllowClanTag(!toggle.isAllowClanTag());
                        account.setToggle(toggle);
                        sound(MenuSound.SUCCESS);
                        handle();
                        TagManager.updateTag(account);
                    }));
        }

    }

    protected void handleStaffPreferences() {
        Toggle toggle = account.getToggle();

        addItem(10, Item.of(Material.PAPER, getColorByToggle(toggle.isStaffChat()) + "Staffchat",
                        "§7Ative ou desative as",
                        "§7mensagens do staffchat.",
                        "",
                        "§7Atual: " + (toggle.isStaffChat() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setStaffChat(!toggle.isStaffChat());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(11, Item.of(Material.NETHER_STAR, getColorByToggle(toggle.isAutoVanish()) + "Auto vanish",
                        "§7Ative ou desative o seu",
                        "§7vanish automático.",
                        "",
                        "§7Atual: " + (toggle.isAutoVanish() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAutoVanish(!toggle.isAutoVanish());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));

        addItem(12, Item.of(Material.CHEST, getColorByToggle(toggle.isAllowLogs()) + "Logs",
                        "§7Ative ou desative as",
                        "§7logs gerais do servidor.",
                        "",
                        "§7Atual: " + (toggle.isAllowLogs() ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    toggle.setAllowLogs(!toggle.isAllowLogs());
                    account.setToggle(toggle);
                    sound(MenuSound.SUCCESS);
                    handle();
                }));
    }
}
