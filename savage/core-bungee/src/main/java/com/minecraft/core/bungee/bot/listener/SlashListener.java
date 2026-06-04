package com.minecraft.core.bungee.bot.listener;

import com.minecraft.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.core.bungee.bot.command.structure.manager.SlashCommandManager;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class SlashListener implements SlashCommandCreateListener {

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        String name = interaction.getCommandName();

        SlashCommand command = SlashCommandManager.of(name);

        if (command != null)
            command.execute(interaction);
    }
}
