package com.minecraft.core.bungee.bot;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bungee.bot.command.structure.manager.SlashCommandManager;
import com.minecraft.core.bungee.bot.listener.ButtonListener;
import com.minecraft.core.bungee.bot.listener.MessageListener;
import com.minecraft.core.bungee.bot.listener.SlashListener;
import lombok.Getter;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.webhook.IncomingWebhook;

@Getter
public class Bot {

    private DiscordApi api;

    public Bot() {
        try {
            new DiscordApiBuilder()
                    .setToken(Constant.DISCORD_BOT_TOKEN)
                    .addIntents(Intent.GUILD_MEMBERS, Intent.MESSAGE_CONTENT)
                    .login()
                    .thenAccept(this::handle)
                    .exceptionally(ex -> {
                        Core.getLogger().warning("[Discord] Não foi possível conectar ao bot no Discord: ada" + ex.getMessage());
                        return null;
                    })
                    .join();
        } catch (Exception e) {
            Core.getLogger().warning("[Discord] Erro ao inicializar o bot no Discord: " + e.getMessage());
        }
    }

    public void handle(DiscordApi api) {
        this.api = api;

        updateActivity();

        api.addSlashCommandCreateListener(new SlashListener());
        api.addButtonClickListener(new ButtonListener());
        api.addMessageCreateListener(new MessageListener());

        Server server = getServer();
        SlashCommandManager.handle(Constant.SOURCE_DIR + ".core.bungee.bot.command.list", api, server);

        // Atualizar atividade a cada 30 segundos
        api.getThreadPool().getScheduler().scheduleAtFixedRate(() -> {
            try {
                updateActivity();
            } catch (Exception e) {
                Core.getLogger().warning("[Discord] Erro ao atualizar atividade do bot: " + e.getMessage());
            }
        }, 30, 30, java.util.concurrent.TimeUnit.SECONDS);

        Core.getLogger().info("[Discord] Os serviços foram iniciados.");
    }
    
    private void updateActivity() {
        if (api == null) {
            return;
        }
        
        try {
            int onlinePlayers = Core.getServerData().getOnlinePlayers();
            String activityText = onlinePlayers + " jogador" + (onlinePlayers != 1 ? "es" : "") + " online";
            
            api.updateActivity(ActivityType.WATCHING, "🌟 savagemc.net | " + activityText);
        } catch (Exception e) {
            // Se houver erro ao obter jogadores, usar mensagem padrão
            api.updateActivity(ActivityType.WATCHING, "🌟 savagemc.net");
        }
    }

    public void disconnect() {
        if (api != null) {
            api.disconnect().join();

            Core.getLogger().info("[Discord] Os serviços foram desligados.");
        }
    }

    /* --------------------------
          Métodos Utilitários
    ---------------------------- */
    public IncomingWebhook getWebhook(String link) {
        return api.getIncomingWebhookByUrl(link).join();
    }

    public TextChannel getChannel(String id) {
        return api.getTextChannelById(id).orElse(null);
    }

    public Role getRole(String id) {
        return api.getRoleById(id).orElse(null);
    }

    public Server getServer() {
        return api.getServerById(Constant.DISCORD_SERVER_ID).orElse(null);
    }
}
