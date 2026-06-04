package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.preference;

import com.minecraft.core.Constant;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.BedMetadata;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.core.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class PreferenceMenu extends Menu {

    private final BedMember member;

    public PreferenceMenu(Player player, BedMember member, Menu last) {
        super(player, "Preferências", last, 5);

        this.member = member;
    }

    @Override
    public void handle() {
        clear();

        BedMetadata metadata = member.getMetadata();

        /* Temporizador da TNT */
        addItem(10, Item.of(Material.TNT, getColorByToggle(metadata.isTntTimerEnabled()) + "Temporizador da TNT",
                        "§7Clique para " + (metadata.isTntTimerEnabled() ? "desativar" : "ativar"),
                        "§7o temporizador da TNT.",
                        "",
                        "§7Estado: " + (metadata.isTntTimerEnabled() ? "§aAtivo" : "§cInativo"),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    metadata.setTntTimerEnabled(!metadata.isTntTimerEnabled());
                    member.saveMetadata(metadata);

                    sound(MenuSound.PAGINATED);
                    handle();
                }));

        addItem(19, toggleButton(metadata.isTntTimerEnabled(), "Temporizador da TNT")
                .click(event -> {
                    metadata.setTntTimerEnabled(!metadata.isTntTimerEnabled());
                    member.saveMetadata(metadata);

                    sound(MenuSound.PAGINATED);
                    handle();
                }));

        /* Tipo de Perfíl */
        ProfileType profile = metadata.getProfile();

        addItem(12, Item.of(profile.equals(ProfileType.RANKED) ? Material.IRON_SWORD : Material.BOW, "§aEstilo de perfíl",
                        "§7Altere o estilo do",
                        "§7seu perfíl para outros jogadores.",
                        "",
                        "§7Usando: §a" + profile.getName(),
                        "",
                        "§eClique para alternar!")
                .flags(ItemFlag.values())
                .click(event -> {
                    ProfileType next = profile.equals(ProfileType.RANKED) ? ProfileType.NORMAL : ProfileType.RANKED;

                    member.setProfile(next);
                    handleSidebarProfileUpdate(member, next);

                    sound(MenuSound.PAGINATED);
                    handle();
                }));

        addItem(21, Item.of(Material.INK_SACK, profile.equals(ProfileType.NORMAL) ? 11 : 1, "§aEstilo de perfíl",
                        "§7Altere o estilo do",
                        "§7seu perfíl para outros jogadores.",
                        "",
                        "§7Usando: §a" + profile.getName(),
                        "",
                        "§eClique para alternar!")
                .click(event -> {
                    ProfileType next = profile.equals(ProfileType.RANKED) ? ProfileType.NORMAL : ProfileType.RANKED;

                    member.setProfile(next);
                    handleSidebarProfileUpdate(member, next);

                    sound(MenuSound.PAGINATED);
                    handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }

    protected void handleSidebarProfileUpdate(BedMember member, ProfileType next) {
        UserModel user = UserModel.of(member.getId());

        if (user != null) {
            Player player = user.getAccount().player();

            if (player != null) {
                Sidebar sidebar = user.getSidebar();

                int xp = member.getLevelXp();
                int minXp = member.getMinLevelXp();
                if (sidebar != null) {
                    sidebar.updateRow("rank", "Seu nível: " + member.getLevelId());

                    sidebar.updateRow("progress", " §8[" + Util.createProgressBar(
                            ChatColor.GREEN, ChatColor.GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10, false) + "]"
                            + " §7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")");
                }

                HologramClient detail = BukkitCore.getManager().getHologram().getClient(player, "stats-details");

                if (detail != null) {
                    detail.setText(0, member.getLevelId());
                    detail.setText(1, "§f["
                            + Util.createProgressBar(ChatColor.GREEN, ChatColor.DARK_GRAY, Constant.SQUARE_SYMBOL, xp, minXp, 10) + "§f] "
                            + "§7(" + Util.formatNumberWithLetter(xp) + "/" + Util.formatNumberWithLetter(minXp) + ")");
                }
            }
        }
    }
}
