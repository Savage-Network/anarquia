package com.minecraft.arcade.event.game.skit.menu.info;

import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.list.serialization.Serialization;
import com.minecraft.arcade.event.game.skit.Skit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SkitInfoMenu extends Menu {

    private final Skit skit;

    public SkitInfoMenu(Player player, Skit skit, Menu last) {
        super(player, "Kit - " + skit.getName(), last, 6);
        this.skit = skit;
    }

    @Override
    public void handle() {
        // Exibir informações do kit de forma simplificada
        getPlayer().sendMessage("§aInformações do kit: §e" + skit.getName());
        getPlayer().sendMessage("§7Autor: " + skit.getAuthorName());
        getPlayer().sendMessage("§cClique para voltar");
    }
}
