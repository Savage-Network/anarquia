package com.minecraft.core.bungee.bot.listener;

import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.bot.Bot;
import com.minecraft.core.bungee.bot.ticket.Ticket;
import com.minecraft.core.bungee.bot.ticket.TicketManager;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.awt.*;
import java.time.Instant;

public class ButtonListener implements ButtonClickListener {
    
    @Override
    public void onButtonClick(ButtonClickEvent event) {
        ButtonInteraction interaction = event.getButtonInteraction();
        String customId = interaction.getCustomId();
        
        Bot bot = BungeeCore.getInstance().getBot();
        if (bot == null) {
            return;
        }
        
        long userId = interaction.getUser().getId();
        
        if (customId.startsWith("claim_")) {
            String ticketId = customId.substring(6);
            Ticket ticket = TicketManager.getTicket(ticketId);
            
            if (ticket == null) {
                interaction.createImmediateResponder()
                        .setContent("❌ Ticket não encontrado!")
                        .respond();
                return;
            }
            
            if (ticket.isClaimed()) {
                interaction.createImmediateResponder()
                        .setContent("❌ Este ticket já foi assumido!")
                        .respond();
                return;
            }
            
            if (!TicketManager.hasStaffRole(userId, bot)) {
                interaction.createImmediateResponder()
                        .setContent("❌ Você não tem permissão para assumir tickets!")
                        .respond();
                return;
            }
            
            if (TicketManager.claimTicket(ticketId, userId, bot)) {
                interaction.createImmediateResponder()
                        .setContent("✅ Ticket assumido com sucesso!")
                        .respond();
            } else {
                interaction.createImmediateResponder()
                        .setContent("❌ Erro ao assumir o ticket!")
                        .respond();
            }
            
        } else if (customId.startsWith("close_")) {
            String ticketId = customId.substring(6);
            Ticket ticket = TicketManager.getTicket(ticketId);
            
            if (ticket == null) {
                interaction.createImmediateResponder()
                        .setContent("❌ Ticket não encontrado!")
                        .respond();
                return;
            }
            
            if (ticket.getStatus() == com.minecraft.core.bungee.bot.ticket.TicketStatus.CLOSED) {
                interaction.createImmediateResponder()
                        .setContent("❌ Este ticket já está fechado!")
                        .respond();
                return;
            }
            
            // Verificar se o usuário pode fechar (criador, quem assumiu, ou staff superior)
            boolean canClose = false;
            
            if (ticket.getCreatorId() == userId) {
                canClose = true;
            } else if (TicketManager.hasStaffRole(userId, bot)) {
                if (!ticket.isClaimed() || ticket.getClaimedBy() == userId || 
                    TicketManager.isSuperior(userId, ticket.getClaimedBy(), bot)) {
                    canClose = true;
                }
            }
            
            if (!canClose) {
                interaction.createImmediateResponder()
                        .setContent("❌ Você não tem permissão para fechar este ticket!")
                        .respond();
                return;
            }
            
            TicketManager.closeTicket(ticketId, userId, bot);
            interaction.createImmediateResponder()
                    .setContent("✅ Ticket fechado com sucesso!")
                    .respond();
        } else if (customId.startsWith("create_ticket_")) {
            String typeName = customId.substring(14);
            com.minecraft.core.bungee.bot.ticket.TicketType type;
            
            try {
                type = com.minecraft.core.bungee.bot.ticket.TicketType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                interaction.createImmediateResponder()
                        .setContent("❌ Tipo de ticket inválido!")
                        .respond();
                return;
            }
            
            Ticket ticket = TicketManager.createTicket(userId, type, bot);
            
            if (ticket != null) {
                interaction.createImmediateResponder()
                        .setContent("✅ Ticket criado com sucesso! Verifique o canal criado.")
                        .respond();
            } else {
                interaction.createImmediateResponder()
                        .setContent("❌ Erro ao criar o ticket!")
                        .respond();
            }
        }
    }
}

