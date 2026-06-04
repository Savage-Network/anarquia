package com.minecraft.core.bungee.bot.command.structure.manager;

import com.minecraft.core.Core;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.core.util.list.loader.ClassLoader;
import lombok.Getter;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class SlashCommandManager {

    @Getter
    private static final Set<SlashCommand> commands = new HashSet<>();

    public static void handle(String commandDir, DiscordApi api, Server server) {
        int loaded = 0;

        Core.getLogger().info("[Slash] Registrando comandos...");

        for (Class<?> commandClass : ClassLoader.getClassesForPackage(BungeeCore.getInstance(), commandDir)) {
            if (SlashCommand.class.isAssignableFrom(commandClass)) {
                try {
                    SlashCommand command = (SlashCommand) commandClass.newInstance();

                    command.builder(api, server);

                    commands.add(command);
                    loaded++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o slash command " + commandClass.getSimpleName(), e);
                }
            }
        }

        if (loaded > 0)
            Core.getLogger().info("[Slash] Registro de comandos concluído. (" + loaded + " comandos registrados)");
    }

    public static SlashCommand of(String name) {
        return commands.stream().filter(slash -> slash.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
