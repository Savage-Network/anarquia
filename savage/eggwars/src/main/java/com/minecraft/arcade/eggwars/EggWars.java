package com.minecraft.arcade.eggwars;

import com.minecraft.arcade.eggwars.packet.SoundPacket;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;

import java.util.Arrays;

public class EggWars extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        getLogger().info("Iniciando EggWars...");

        saveDefaultConfig();

        /* Configurações de Servidor */
        ServerOptions.BLOCK_PLACE = true;
        ServerOptions.BLOCK_BREAK = true;

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.DAMAGE_ENABLED = true;

        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.AUTO_CLEAR_DROPS = false;

        ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE = false;

        ServerOptions.BLOCK_INTERACTION_ENABLED = true;
        ServerOptions.NON_INTERACTIVE_BLOCKS = Arrays.asList(Material.DRAGON_EGG);

        ServerOptions.SPAWN_CREATURES = false;

        Core.setServerType(ServerType.EGGWARS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        /* Iniciando jogos */
        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.eggwars.arcade.list");

        /* Iniciando comandos & eventos */
        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.eggwars.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.eggwars.listener");

        // Packets
        getManager().getProtocol().addPacketListener(new SoundPacket(this));

        getLogger().info("EggWars iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("EggWars desligado com sucesso.");
    }
}
