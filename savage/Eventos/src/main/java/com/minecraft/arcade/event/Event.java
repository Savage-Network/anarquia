package com.minecraft.arcade.event;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.api.item.Item;
import com.minecraft.arcade.event.controller.SkitController;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.variable.Variable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Objects;

public class Event extends BukkitCore {

    @Getter
    private static Game game;

    @Override
    public void onLoad() {
        super.onLoad();

        Core.setServerType(ServerType.EVENTOS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Configurar ServerOptions após o plugin estar carregado
        ServerOptions.DAMAGE_ENABLED = true;
        ServerOptions.FOOD_ENABLED = false;
        ServerOptions.DROP_ITEM_ENABLED = false;
        ServerOptions.CHAT_ENABLED = true;
        ServerOptions.BLOCK_PLACE = false;
        ServerOptions.BLOCK_BREAK = false;
        ServerOptions.BUCKET_ENABLED = false;
        ServerOptions.BLOCK_INTERACTION_ENABLED = true;

        game = new Game();

        this.handleRecipes();

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".event.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".event.listener");

        getLogger().info("Plugin iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        SkitController.clear();

        super.onDisable();
    }

    protected void handleRecipes() {
        ShapelessRecipe cocoaSoup = new ShapelessRecipe(Item.of(Material.MUSHROOM_SOUP))
                .addIngredient(Material.BOWL)
                .addIngredient(Material.INK_SACK, 3);
        Bukkit.addRecipe(cocoaSoup);

        ShapelessRecipe cactusSoup = new ShapelessRecipe(Item.of(Material.MUSHROOM_SOUP))
                .addIngredient(Material.BOWL)
                .addIngredient(Material.CACTUS);

        Bukkit.addRecipe(cactusSoup);

        ShapelessRecipe flowerSoup = new ShapelessRecipe(Item.of(Material.MUSHROOM_SOUP))
                .addIngredient(Material.BOWL)
                .addIngredient(Material.RED_ROSE)
                .addIngredient(Material.YELLOW_FLOWER);

        Bukkit.addRecipe(flowerSoup);
    }
}
