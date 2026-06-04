package com.minecraft.proxyhost.backend.redis;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.config.ConfigSection;
import lombok.Getter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Inicializacao lazy do Jedis. Quando desabilitado, todas as operacoes viram no-op.
 * No proxy, o Redis e usado para receber mensagens cross-server (tell, staffchat).
 */
public final class RedisProvider {

    private final ConfigSection config;

    @Getter private boolean enabled;
    @Getter private JedisPool pool;
    private final AtomicBoolean subscribed = new AtomicBoolean(false);

    public RedisProvider(ConfigSection config) {
        this.config = config;
    }

    public void init() {
        this.enabled = config.getBoolean("redis.enabled", false);
        if (!enabled) {
            Core.getLogger().info("Redis desabilitado (config).");
            return;
        }

        String host = config.getString("redis.host", "127.0.0.1");
        int port = config.getInt("redis.port", 6379);
        int database = 0;
        boolean ssl = false;
        String password = config.getString("redis.password", "");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(4);

        if (password == null || password.isEmpty()) {
            this.pool = new JedisPool(poolConfig, host, port, 2000, null, database, ssl);
        } else {
            this.pool = new JedisPool(poolConfig, host, port, 2000, password, database, ssl);
        }

        Core.getLogger().info("Redis conectado em " + host + ":" + port + " (db=" + database + ").");
    }

    public void publish(String channel, String message) {
        if (!enabled || pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.publish(channel, message);
        } catch (Throwable t) {
            Core.getLogger().warn("Falha ao publicar no Redis: " + t.getMessage());
        }
    }

    public void setEx(String key, String value, int seconds) {
        if (!enabled || pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.setex(key, seconds, value);
        } catch (Throwable t) {
            Core.getLogger().warn("Falha ao gravar Redis key " + key + ": " + t.getMessage());
        }
    }

    /** Inicia uma thread daemon que escuta nos canais informados. */
    public void subscribe(JedisPubSub handler, String... channels) {
        if (!enabled || pool == null) return;
        Thread t = new Thread(() -> {
            try (var jedis = pool.getResource()) {
                jedis.subscribe(handler, channels);
            } catch (Throwable err) {
                Core.getLogger().warn("Redis subscribe encerrado: " + err.getMessage());
            }
        }, "proxyhost-redis-subscriber");
        t.setDaemon(true);
        t.start();
        subscribed.set(true);
    }

    public void shutdown() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }
}
