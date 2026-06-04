package com.minecraft.core.bungee;

import com.minecraft.core.Core;
import com.minecraft.core.SavageNetworkProducts;
import com.minecraft.core.bungee.server.BungeeServer;
import com.minecraft.core.Constant;
import com.minecraft.core.backend.database.redis.listener.RedisListener;
import com.minecraft.core.bungee.command.structure.BungeeCommandHandler;
import com.minecraft.core.bungee.listener.handler.ListenerHandler;
import com.minecraft.core.bungee.service.filter.BungeeLogFilter;
import com.minecraft.core.bungee.service.redis.BungeeRedis;
import com.minecraft.core.bungee.service.task.UpdateTask;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class BungeeCore extends Plugin {

    @Getter
    private static BungeeCore instance;

    private RedisListener redisListener;

    @Override
    public void onLoad() {
        getLogger().info(SavageNetworkProducts.proxyStartupBanner());
        getLogger().info("A carregar core proxy...");

        instance = this;

        new BungeeLogFilter().registerFilter();

        Core.load(new BungeePlatform(this), new BungeeServer(), getLogger());

        // Limpar dados de forma assíncrona após o Redis estar pronto
        Core.getPlatform().runAsync(() -> {
            try {
                // Aguardar um pouco para garantir que o Redis está pronto
                Thread.sleep(500);
                
                if (Core.getRedis().isAvailable()) {
                    Core.getPartyData().clear();
                    Core.getAccountData().clearCache();
                } else {
                    getLogger().warning("Redis não está disponível, pulando limpeza de cache.");
                }
            } catch (Exception e) {
                getLogger().warning("Erro ao limpar cache: " + e.getMessage());
            }
        });
    }

    @Override
    public void onEnable() {
        // Executar start de forma assíncrona para não bloquear a thread principal
        Core.getPlatform().runAsync(() -> {
            try {
                Core.getServerData().start(2024);
            } catch (Exception e) {
                getLogger().severe("Erro ao iniciar ServerData: " + e.getMessage());
                e.printStackTrace();
            }
        });

        new BungeeCommandHandler(this).handle(Constant.SOURCE_DIR + ".core.bungee.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".core.bungee");

        redisListener = new RedisListener(new BungeeRedis(), Constant.REDIS_CHANNELS);
        Core.getPlatform().runAsync(() -> redisListener.load());

        getProxy().getScheduler().schedule(this, new UpdateTask(), 0, 1, TimeUnit.SECONDS);

        getLogger().info("Core proxy ativo (" + SavageNetworkProducts.MONKEYCORD + ").");
        getLogger().info("Bot Discord: processo separado (discord-bot). URL: " +
                (System.getenv("SAVAGE_BOT_URL") != null ? System.getenv("SAVAGE_BOT_URL") : "http://127.0.0.1:4040"));
    }

    @Override
    public void onDisable() {
        Core.getSkinCacheData().clear();
        
        if (redisListener != null) {
            redisListener.unload();
        }
        
        // Remover o servidor do Redis antes de desligar
        try {
            Core.getServerData().stop();
            Core.getLogger().info("[BungeeCore] Servidor removido do Redis com sucesso.");
        } catch (Exception e) {
            Core.getLogger().severe("[BungeeCore] Erro ao remover servidor do Redis: " + e.getMessage());
        }

        Core.unload();
    }
}
