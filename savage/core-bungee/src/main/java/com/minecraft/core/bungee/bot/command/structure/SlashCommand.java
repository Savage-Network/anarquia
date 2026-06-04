package com.minecraft.core.bungee.bot.command.structure;

import com.minecraft.core.Core;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.bot.command.structure.category.SlashCommandCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
@AllArgsConstructor
public abstract class SlashCommand {

    private final String name;
    private final String description;

    private final SlashCommandCategory category;

    private final List<SlashCommandOption> options = new ArrayList<>();

    public abstract void execute(SlashCommandInteraction interaction);

    public SlashCommandBuilder builder(org.javacord.api.DiscordApi api, org.javacord.api.entity.server.Server server) {
        SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName(name)
                .setDescription(description);

        options.forEach(builder::addOption);

        if (category.equals(SlashCommandCategory.GLOBAL))
            builder.createGlobal(api)
                    .thenAccept(command -> Core.getLogger().info("[Slash/Global] Comando \"" + command.getName() + "\" registrado."))
                    .exceptionally(e -> {
                        Core.getLogger().log(Level.SEVERE, "[Slash/Global] Não foi possível registrar o comando...", e);
                        return null;
                    });
        else {
            if (server != null) {
                builder.createForServer(server)
                        .thenAccept(command -> Core.getLogger().info("[Slash/Server] Comando \"" + command.getName() + "\" registrado."))
                        .exceptionally(e -> {
                            Core.getLogger().log(Level.SEVERE, "[Slash/Server] Não foi possível registrar o comando...", e);
                            return null;
                        });
            } else {
                Core.getLogger().warning("[Slash/Server] Servidor não encontrado. Comando \"" + name + "\" não foi registrado.");
            }
        }

        return builder;
    }
}
