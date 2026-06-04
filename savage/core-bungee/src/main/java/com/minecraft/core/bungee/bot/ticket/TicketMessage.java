package com.minecraft.core.bungee.bot.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class TicketMessage {
    private long authorId;
    private String content;
    private Instant timestamp;
}

