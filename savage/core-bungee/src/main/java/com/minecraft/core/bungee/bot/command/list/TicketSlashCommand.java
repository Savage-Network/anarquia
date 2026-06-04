package com.minecraft.core.bungee.bot.command.list;

import com.minecraft.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.core.bungee.bot.command.structure.category.SlashCommandCategory;
import com.minecraft.core.bungee.bot.command.structure.embed.SlashEmbed;
import com.minecraft.core.bungee.bot.ticket.TicketType;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.awt.*;

public class TicketSlashCommand extends SlashCommand {
    
    public TicketSlashCommand() {
        super("ticket", "Criar um painel de tickets", SlashCommandCategory.SERVER);
    }
    
    @Override
    public void execute(SlashCommandInteraction interaction) {
        EmbedBuilder embed = new SlashEmbed()
                .setTitle("Central de Atendimento")
                .setDescription("> Caso necessite de suporte, clique no botão abaixo para abrir um ticket e solicitar atendimento diretamente com o nosso time de suporte.\n\n`Pedimos que utilize este recurso apenas para solicitações que realmente exijam atendimento individual.`")
                .addField("Horário de Atendimento", "🗓️ Segunda à Sexta: **10h00** às **21h**.\n🗓️ Finais de semana e feriado: **14h00** às **21h**.")
                .setColor(Color.GRAY);
        
        interaction.createImmediateResponder()
                .addEmbed(embed)
                .addComponents(
                        new ActionRowBuilder()
                                .addComponents(
                                        new ButtonBuilder()
                                                .setStyle(ButtonStyle.PRIMARY)
                                                .setCustomId("create_ticket_SUPPORT")
                                                .setLabel(TicketType.SUPPORT.getDisplayName())
                                                .build(),
                                        new ButtonBuilder()
                                                .setStyle(ButtonStyle.PRIMARY)
                                                .setCustomId("create_ticket_BUG_REPORT")
                                                .setLabel(TicketType.BUG_REPORT.getDisplayName())
                                                .build(),
                                        new ButtonBuilder()
                                                .setStyle(ButtonStyle.PRIMARY)
                                                .setCustomId("create_ticket_APPEAL")
                                                .setLabel(TicketType.APPEAL.getDisplayName())
                                                .build(),
                                        new ButtonBuilder()
                                                .setStyle(ButtonStyle.PRIMARY)
                                                .setCustomId("create_ticket_APPEAL")
                                                .setLabel(TicketType.APPEAL.getDisplayName())
                                                .build(),
                                        new ButtonBuilder()
                                                .setStyle(ButtonStyle.SECONDARY)
                                                .setCustomId("create_ticket_OTHER")
                                                .setLabel(TicketType.OTHER.getDisplayName())
                                                .build()
                                ).build()).respond();
    }
}

