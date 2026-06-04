package com.minecraft.lobby;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.lobby.architect.loader.ArchitectLoader;
import lombok.Getter;
import org.bukkit.Bukkit;

public class Lobby extends BukkitCore {

    @Getter
    private static ArchitectLoader loader;

    @Override
    public void onLoad() {
        super.onLoad();

        loader = new ArchitectLoader(this);

        getLogger().info("Iniciando Lobby...");

        Core.setServerType(loader.getArchitect().getServer());

        ServerOptions.SPAWN_CREATURES = true;
        ServerOptions.DEFAULT_CHAT = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (loader.getArchitect() == null) {
            getLogger().severe("Não foi possível carregar as configurações...");

            Bukkit.shutdown();
        } else
            loader.getArchitect().load();

        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".lobby.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".lobby");

        // Inicializar holograma do Discord
        try {
            com.minecraft.arcade.lobby.manager.DiscordHologramManager.initialize(this);
        } catch (Exception e) {
            getLogger().warning("Não foi possível inicializar o holograma do Discord: " + e.getMessage());
        }

        getLogger().info("Lobby iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Desligar holograma do Discord
        try {
            com.minecraft.arcade.lobby.manager.DiscordHologramManager.shutdown();
        } catch (Exception e) {
            getLogger().warning("Erro ao desligar holograma do Discord: " + e.getMessage());
        }

        loader.getArchitect().unload();

        getLogger().info("Lobby encerrado com sucesso.");
    }
}
