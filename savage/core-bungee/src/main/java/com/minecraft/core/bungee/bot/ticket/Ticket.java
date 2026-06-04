package com.minecraft.core.bungee.bot.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Ticket {
    
    private String id;
    private long channelId;
    private long creatorId;
    private Long claimedBy; // null se não foi assumido
    private TicketType type;
    private TicketStatus status;
    private Instant createdAt;
    private Instant closedAt;
    private List<TicketMessage> messages;
    
    public Ticket(String id, long channelId, long creatorId, TicketType type) {
        this.id = id;
        this.channelId = channelId;
        this.creatorId = creatorId;
        this.claimedBy = null;
        this.type = type;
        this.status = TicketStatus.OPEN;
        this.createdAt = Instant.now();
        this.closedAt = null;
        this.messages = new ArrayList<>();
    }
    
    public boolean isClaimed() {
        return claimedBy != null;
    }
    
    public boolean canInteract(long userId, boolean isSuperior) {
        if (status != TicketStatus.OPEN) {
            return false;
        }
        
        if (creatorId == userId) {
            return true;
        }
        
        if (isClaimed()) {
            return claimedBy == userId || isSuperior;
        }
        
        return true; // Qualquer staff pode ver tickets não assumidos
    }
    
    public void addMessage(long authorId, String content, Instant timestamp) {
        messages.add(new TicketMessage(authorId, content, timestamp));
    }
}

