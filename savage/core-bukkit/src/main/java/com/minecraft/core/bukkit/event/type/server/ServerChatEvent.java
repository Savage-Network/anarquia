package com.minecraft.core.bukkit.event.type.server;

import com.minecraft.core.bukkit.event.EventHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ServerChatEvent extends EventHandler {

    private final Player sender;
    private final String message;

    private final List<Player> recipients;

    private boolean cancelled = false;

    public void send(String... message) {
        recipients.forEach(recipient -> recipient.sendMessage(message));
    }

    public void send(BaseComponent... message) {
        recipients.forEach(recipient -> recipient.sendMessage(message));
    }
}
