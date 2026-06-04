package com.minecraft.arcade.bedwars.menu.custom.preference;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShopConfigMenu extends Menu {

    private final Arena arena;

    public ShopConfigMenu(Player player, Arena arena, Menu last) {
        super(player, "Configurar Loja", last, 3);

        this.arena = arena;
    }

    @Override
    public void handle() {
        clear();

        RoomCustom custom = arena.getCustom();
        boolean freeShop = custom.getShopConfig().isFreeShop();

        addItem(11, Item.of(Material.EMERALD, "§aTudo de Graça")
                .lore("§7Ative para deixar todos",
                        "§7os itens da loja gratuitos.",
                        "",
                        "§7Status: " + (freeShop ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para " + (freeShop ? "desativar" : "ativar") + "!")
                .click(event -> {
                    custom.getShopConfig().setFreeShop(!freeShop);
                    sound(MenuSound.DONE);
                    handle();
                }));

        addItem(13, Item.of(Material.SKULL_ITEM, 3, "§eConfigurar Itens")
                .skullByBase64("ewogICJ0aW1lc3RhbXAiIDogMTYxNjI0NjA0ODM4MCwKICAicHJvZmlsZUlkIiA6ICJjNjc3MGJjZWMzZjE0ODA3ODc4MTU0NWRhMGFmMDI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUxYjhlN2FmNGU1NDZlNzFiNGU4N2MxY2Y5NzNjYmM2MDVkN2ZkZDI5ZWE4Y2JhNTI1ZmM1ZmY4MzQ1MTMwNCIKICAgIH0KICB9Cn0=")
                .lore("§7Configure preços, moedas",
                        "§7e quantidades dos itens.",
                        "",
                        "§eClique para abrir!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ShopItemCategoryMenu(getPlayer(), arena, this).handle();
                }));

        addItem(15, Item.of(Material.BARRIER, "§cResetar Configurações")
                .lore("§7Reseta todas as configurações",
                        "§7da loja para o padrão.",
                        "",
                        "§cClique para resetar!")
                .click(event -> {
                    custom.getShopConfig().resetAll();
                    sound(MenuSound.DONE);
                    getPlayer().sendMessage("§aConfigurações da loja resetadas!");
                    handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
