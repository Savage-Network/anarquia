package com.minecraft.core.bungee.bot.listener;

import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.bot.Bot;
import com.minecraft.core.bungee.bot.ticket.Ticket;
import com.minecraft.core.bungee.bot.ticket.TicketManager;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.time.Instant;

public class MessageListener implements MessageCreateListener {
    
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();
        
        if (message.getAuthor().isBotUser()) {
            return;
        }
        
        if (!(message.getChannel() instanceof ServerTextChannel)) {
            return;
        }
        
        ServerTextChannel channel = (ServerTextChannel) message.getChannel();
        Ticket ticket = TicketManager.getTicketByChannel(channel.getId());
        
        if (ticket == null) {
            return;
        }
        
        // Verificar se o ticket está fechado
        if (ticket.getStatus() == com.minecraft.core.bungee.bot.ticket.TicketStatus.CLOSED) {
            message.delete().exceptionally(e -> null);
            return;
        }
        
        // Verificar permissões
        Bot bot = BungeeCore.getInstance().getBot();
        if (bot == null) {
            return;
        }
        
        long userId = message.getAuthor().getId();
        boolean isSuperior = TicketManager.isSuperior(userId, ticket.getClaimedBy() != null ? ticket.getClaimedBy() : 0, bot);
        boolean hasStaffRole = TicketManager.hasStaffRole(userId, bot);
        
        // Se o ticket está assumido, apenas quem assumiu, superiores ou o criador podem enviar mensagens
        if (ticket.isClaimed()) {
            boolean canSend = ticket.getCreatorId() == userId || 
                           ticket.getClaimedBy() == userId || 
                           (hasStaffRole && isSuperior);
            
            if (!canSend) {
                message.delete().exceptionally(e -> null);
                channel.sendMessage("❌ Apenas quem assumiu este ticket ou superiores podem enviar mensagens aqui!")
                        .exceptionally(e -> null);
                return;
            }
        }
        
        // Registrar mensagem
        String content = message.getContent();
        if (content != null && !content.isEmpty()) {
            ticket.addMessage(userId, content, Instant.now());
        }
    }
}

