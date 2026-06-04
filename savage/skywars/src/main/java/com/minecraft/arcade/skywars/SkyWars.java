package com.minecraft.arcade.skywars;

import com.minecraft.arcade.skywars.structure.chest.LootManager;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;

import java.util.Arrays;

public class SkyWars extends BukkitCore {

    private static SkyWars instance;

    public static SkyWars getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        super.onLoad();

        getLogger().info("Iniciando SkyWars...");

        try {
            saveDefaultConfig();
        } catch (IllegalArgumentException e) {
            getLogger().warning("config.yml não encontrado no JAR. Criando arquivo padrão...");
        }

        /* Configurações de Servidor */
        ServerOptions.BLOCK_PLACE = true;
        ServerOptions.BLOCK_BREAK = true;

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.DAMAGE_ENABLED = true;

        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.AUTO_CLEAR_DROPS = false;

        ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE = false;

        ServerOptions.BLOCK_INTERACTION_ENABLED = true;
        ServerOptions.NON_INTERACTIVE_BLOCKS = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST);

        ServerOptions.SPAWN_CREATURES = true;

        Core.setServerType(ServerType.SKYWARS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        /* Carregar loot */
        LootManager.load();

        /* Iniciando jogos */
        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.skywars.arcade.list");

        /* Iniciando comandos & eventos */
        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.skywars.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.skywars.listener");

        getLogger().info("SkyWars iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("SkyWars desligado com sucesso.");
    }
}

