package com.minecraft.core.bukkit.event.type.server;

import com.minecraft.core.Core;
import com.minecraft.core.bukkit.event.EventHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;

@Getter
public class ServerRedisMessageEvent extends EventHandler {

    private final String channel;
    private final String message;

    private final JsonObject json;

    public ServerRedisMessageEvent(String channel, String message) {
        this.channel = channel;
        this.message = message;

        JsonObject parsedJson = null;
        
        try {
            parsedJson = Core.PARSER.parse(message).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            Core.getLogger().warning("[Redis] Erro ao fazer parse da mensagem do canal '" + channel + "': " + e.getMessage());
            Core.getLogger().warning("[Redis] Mensagem recebida: " + (message != null && message.length() > 200 ? message.substring(0, 200) + "..." : message));
        } catch (Exception e) {
            Core.getLogger().severe("[Redis] Erro inesperado ao processar mensagem do canal '" + channel + "': " + e.getMessage());
            e.printStackTrace();
        }
        
        this.json = parsedJson;
    }
}
