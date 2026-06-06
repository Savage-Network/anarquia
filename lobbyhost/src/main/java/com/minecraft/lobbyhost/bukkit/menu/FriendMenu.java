package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.friend.Friend;
import com.minecraft.lobbyhost.social.FriendService;
import com.minecraft.lobbyhost.message.MenuTexts;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class FriendMenu {

    private static final int SIZE = 45;
    private static final int CLOSE_SLOT = 40;

    private FriendMenu() {}

    public static void open(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            player.sendMessage(ModernText.parse(MenuTexts.mini(player, "menus.lobby.account-error")));
            return;
        }
        LobbyMenuHolder holder = new LobbyMenuHolder("friends");
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                MenuTexts.title(player, "menus.friends.title"));
        holder.bind(inv);

        int slot = 10;
        for (Friend f : FriendService.listFriends(account)) {
            if (slot > 34) break;
            boolean online = Bukkit.getPlayer(f.getId()) != null;
            inv.setItem(slot++, friendHead(f, online));
        }

        var requests = account.getFriendMetadata().getRequests();
        if (requests != null && !requests.isEmpty()) {
            inv.setItem(4, labeledMini(Material.PAPER,
                    ModernText.gradient("Pedidos pendentes", ModernText.WARN, ModernText.BRAND_B),
                    List.of(
                            ModernText.mini(requests.size() + " pedido(s)"),
                            ModernText.hint("amigo aceitar <nick>")
                    )));
        }

        inv.setItem(22, labeledMini(Material.EMERALD,
                ModernText.gradient("Adicionar amigo", ModernText.SUCCESS, ModernText.ACCENT_A),
                List.of(ModernText.hint("amigo add <nick>"))));
        inv.setItem(CLOSE_SLOT, labeledMini(Material.BARRIER,
                "<!italic><" + ModernText.ERROR + "><bold>Fechar</bold>", List.of()));
        player.openInventory(inv);
    }

    private static ItemStack friendHead(Friend f, boolean online) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (f.getName() != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(f.getId()));
        }
        String color = online ? ModernText.SUCCESS : ModernText.MUTED;
        meta.displayName(ModernText.noItalic(ModernText.parse(
                "<!italic><" + color + ">" + f.getName())));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(ModernText.noItalic(ModernText.parse(online
                ? "<!italic><" + ModernText.SUCCESS + ">Online no lobby"
                : ModernText.mini("Offline"))));
        lore.add(ModernText.noItalic(ModernText.parse(ModernText.hint("amigo remover " + f.getName()))));
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack labeledMini(Material material, String miniName, List<String> miniLore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.noItalic(ModernText.parse(miniName)));
        if (miniLore != null) {
            meta.lore(miniLore.stream().map(l -> ModernText.noItalic(ModernText.parse(l))).toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
