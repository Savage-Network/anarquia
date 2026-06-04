package com.minecraft.core.bungee.bot.ticket;

import com.minecraft.core.Core;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.bungee.bot.Bot;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TicketManager {
    
    private static final String TICKET_CATEGORY_ID = "1448897115072761968";
    private static final String STAFF_ROLE_ID = "1440434723225600133";
    private static final String LOGS_CHANNEL_ID = "1442275587304587464";
    
    private static final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private static final Map<Long, String> channelToTicket = new ConcurrentHashMap<>();
    
    public static Ticket createTicket(long creatorId, TicketType type, Bot bot) {
        Server server = bot.getServer();
        if (server == null) {
            return null;
        }
        
        User creator = bot.getApi().getUserById(creatorId).join();
        if (creator == null) {
            return null;
        }
        
        ChannelCategory category = server.getChannelCategoryById(TICKET_CATEGORY_ID).orElse(null);
        if (category == null) {
            Core.getLogger().warning("[Ticket] Categoria de tickets não encontrada!");
            return null;
        }
        
        Role staffRole = bot.getRole(STAFF_ROLE_ID);
        if (staffRole == null) {
            Core.getLogger().warning("[Ticket] Cargo de staff não encontrado!");
            return null;
        }
        
        String ticketId = "ticket-" + System.currentTimeMillis();
        String channelName = type.getEmoji() + "-" + creator.getName().toLowerCase();
        
        try {
            // Criar o canal (usará permissões padrão da categoria)
            ServerTextChannel channel = new ServerTextChannelBuilder(server)
                    .setName(channelName)
                    .setCategory(category)
                    .create()
                    .join();
            
            Ticket ticket = new Ticket(ticketId, channel.getId(), creatorId, type);
            tickets.put(ticketId, ticket);
            channelToTicket.put(channel.getId(), ticketId);
            
            // Enviar mensagem inicial
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎫 Ticket Criado")
                    .setDescription("Olá " + creator.getMentionTag() + "! Seu ticket foi criado com sucesso.")
                    .addField("Tipo", type.getEmoji() + " " + type.getDisplayName(), false)
                    .addField("Status", "Aguardando atendimento", false)
                    .setColor(Color.GREEN)
                    .setTimestamp(Instant.now());
            
            channel.sendMessage(embed).join();
            
            // Enviar mensagem com botões
            EmbedBuilder controlEmbed = new EmbedBuilder()
                    .setTitle("🎫 Controles do Ticket")
                    .setDescription("Use os botões abaixo para gerenciar este ticket.")
                    .setColor(Color.BLUE)
                    .setTimestamp(Instant.now());
            
            new org.javacord.api.entity.message.MessageBuilder()
                    .setEmbed(controlEmbed)
                    .addComponents(
                            new ActionRowBuilder()
                                    .addComponents(
                                            new ButtonBuilder()
                                                    .setStyle(ButtonStyle.PRIMARY)
                                                    .setCustomId("claim_" + ticket.getId())
                                                    .setLabel("Assumir Ticket")
                                                    .build(),
                                            new ButtonBuilder()
                                                    .setStyle(ButtonStyle.DANGER)
                                                    .setCustomId("close_" + ticket.getId())
                                                    .setLabel("Fechar Ticket")
                                                    .build()
                                    )
                                    .build()
                    )
                    .send(channel)
                    .join();
            
            return ticket;
            
        } catch (Exception e) {
            Core.getLogger().warning("[Ticket] Erro ao criar canal do ticket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static Ticket getTicket(String ticketId) {
        return tickets.get(ticketId);
    }
    
    public static Ticket getTicketByChannel(long channelId) {
        String ticketId = channelToTicket.get(channelId);
        return ticketId != null ? tickets.get(ticketId) : null;
    }
    
    public static boolean claimTicket(String ticketId, long userId, Bot bot) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null || ticket.isClaimed()) {
            return false;
        }
        
        ticket.setClaimedBy(userId);
        ticket.setStatus(TicketStatus.CLAIMED);
        
        ServerTextChannel channel = bot.getApi().getServerTextChannelById(ticket.getChannelId()).orElse(null);
        if (channel != null) {
            User claimer = bot.getApi().getUserById(userId).join();
            if (claimer != null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("✅ Ticket Assumido")
                        .setDescription("O ticket foi assumido por " + claimer.getMentionTag())
                        .setColor(Color.ORANGE)
                        .setTimestamp(Instant.now());
                
                channel.sendMessage(embed).join();
            }
        }
        
        return true;
    }
    
    public static void closeTicket(String ticketId, long closerId, Bot bot) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) {
            return;
        }
        
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        
        ServerTextChannel channel = bot.getApi().getServerTextChannelById(ticket.getChannelId()).orElse(null);
        if (channel != null) {
            User closer = bot.getApi().getUserById(closerId).join();
            if (closer != null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🔒 Ticket Fechado")
                        .setDescription("Este ticket foi fechado por " + closer.getMentionTag())
                        .setColor(Color.RED)
                        .setTimestamp(Instant.now());
                
                channel.sendMessage(embed).join();
            }
            
            // Enviar logs
            sendTicketLogs(ticket, bot);
            
            // Deletar canal após 5 segundos
            bot.getApi().getThreadPool().getScheduler().schedule(() -> {
                try {
                    channel.delete().join();
                    Core.getLogger().info("[Ticket] Canal do ticket " + ticket.getId() + " deletado com sucesso.");
                } catch (Exception e) {
                    Core.getLogger().warning("[Ticket] Erro ao deletar canal do ticket: " + e.getMessage());
                    e.printStackTrace();
                }
            }, 5, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        // Remover do cache
        tickets.remove(ticketId);
        channelToTicket.remove(ticket.getChannelId());
    }
    
    private static void sendTicketLogs(Ticket ticket, Bot bot) {
        org.javacord.api.entity.channel.TextChannel logsChannel = bot.getChannel(LOGS_CHANNEL_ID);
        if (logsChannel == null || !(logsChannel instanceof ServerTextChannel)) {
            Core.getLogger().warning("[Ticket] Canal de logs não encontrado!");
            return;
        }
        
        ServerTextChannel serverLogsChannel = (ServerTextChannel) logsChannel;
        
        try {
            User creator = bot.getApi().getUserById(ticket.getCreatorId()).join();
            User claimedBy = ticket.getClaimedBy() != null ? 
                    bot.getApi().getUserById(ticket.getClaimedBy()).join() : null;
            
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(java.time.ZoneId.systemDefault());
            
            // Criar conteúdo do arquivo de log
            StringBuilder logContent = new StringBuilder();
            logContent.append("═══════════════════════════════════════════════════════════\n");
            logContent.append("📋 LOGS DO TICKET #").append(ticket.getId()).append("\n");
            logContent.append("═══════════════════════════════════════════════════════════\n\n");
            logContent.append("Criador: ").append(creator != null ? creator.getName() + " (" + creator.getDiscriminatedName() + ")" : "Desconhecido").append("\n");
            logContent.append("Tipo: ").append(ticket.getType().getEmoji()).append(" ").append(ticket.getType().getDisplayName()).append("\n");
            logContent.append("Assumido por: ").append(claimedBy != null ? claimedBy.getName() + " (" + claimedBy.getDiscriminatedName() + ")" : "Ninguém").append("\n");
            logContent.append("Criado em: ").append(formatter.format(ticket.getCreatedAt())).append("\n");
            logContent.append("Fechado em: ").append(ticket.getClosedAt() != null ? formatter.format(ticket.getClosedAt()) : "N/A").append("\n");
            logContent.append("\n");
            logContent.append("═══════════════════════════════════════════════════════════\n");
            logContent.append("MENSAGENS:\n");
            logContent.append("═══════════════════════════════════════════════════════════\n\n");
            
            if (ticket.getMessages().isEmpty()) {
                logContent.append("Nenhuma mensagem registrada.\n");
            } else {
                for (TicketMessage msg : ticket.getMessages()) {
                    User author = bot.getApi().getUserById(msg.getAuthorId()).join();
                    String authorName = author != null ? author.getName() + " (" + author.getDiscriminatedName() + ")" : "Desconhecido";
                    logContent.append("[").append(formatter.format(msg.getTimestamp())).append("] ")
                            .append(authorName).append(": ").append(msg.getContent()).append("\n");
                }
            }
            
            // Criar arquivo temporário
            java.io.File tempFile = java.io.File.createTempFile("ticket-log-" + ticket.getId() + "-", ".txt");
            tempFile.deleteOnExit();
            
            try (java.io.FileWriter writer = new java.io.FileWriter(tempFile, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(logContent.toString());
            }
            
            // Enviar embed com informações resumidas e arquivo anexado
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("📋 Logs do Ticket #" + ticket.getId())
                    .addField("Criador", creator != null ? creator.getMentionTag() : "Desconhecido", true)
                    .addField("Tipo", ticket.getType().getEmoji() + " " + ticket.getType().getDisplayName(), true)
                    .addField("Assumido por", claimedBy != null ? claimedBy.getMentionTag() : "Ninguém", true)
                    .addField("Criado em", formatter.format(ticket.getCreatedAt()), false)
                    .addField("Fechado em", ticket.getClosedAt() != null ? 
                            formatter.format(ticket.getClosedAt()) : "N/A", false)
                    .addField("Total de mensagens", String.valueOf(ticket.getMessages().size()), true)
                    .setColor(Color.MAGENTA)
                    .setTimestamp(Instant.now());
            
            new org.javacord.api.entity.message.MessageBuilder()
                    .setEmbed(embed)
                    .addAttachment(tempFile)
                    .send(serverLogsChannel)
                    .thenRun(() -> {
                        // Deletar arquivo temporário após enviar
                        tempFile.delete();
                    })
                    .join();
            
        } catch (Exception e) {
            Core.getLogger().warning("[Ticket] Erro ao enviar logs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean hasStaffRole(long userId, Bot bot) {
        Server server = bot.getServer();
        if (server == null) {
            return false;
        }
        
        Role staffRole = bot.getRole(STAFF_ROLE_ID);
        if (staffRole == null) {
            return false;
        }
        
        return server.getMemberById(userId)
                .map(member -> member.getRoles(server).contains(staffRole))
                .orElse(false);
    }
    
    public static boolean isSuperior(long userId1, long userId2, Bot bot) {
        // Implementar lógica de hierarquia se necessário
        // Por enquanto, retorna false (mesmo nível)
        return false;
    }
}

