package com.minecraft.core.bungee.bot.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketType {
    SUPPORT("Dúvidas Gerais", "❓"),
    BUG_REPORT("Reportar Bug", "⚠️"),
    APPEAL("Revisão de Punição", "🔨"),
    OTHER("Outro", "⚫");
    
    private final String displayName;
    private final String emoji;
}

