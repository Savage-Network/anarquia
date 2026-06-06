package com.minecraft.eventos.backend.redis;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

/**
 * Inicializacao lazy do Jedis. Quando {@code redis.enabled = false}, o
 * pool permanece {@code null} e {@link #isEnabled()} retorna falso -
 * as features de cross-server fallback para no-op.
 */
public final class RedisProvider {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    @Getter
    private boolean enabled;

    @Getter
    private JedisPool pool;

    public RedisProvider(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void init() {
        this.enabled = config.getBoolean("redis.enabled", false);
        if (!enabled) {
            plugin.getLogger().info("Redis desabilitado (config).");
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

        plugin.getLogger().info("Redis conectado em " + host + ":" + port + " (db=" + database + ").");
    }

    public void publish(String channel, String message) {
        if (!enabled || pool == null) return;
        try (var jedis = pool.getResource()) {
            jedis.publish(channel, message);
        } catch (Throwable t) {
            plugin.getLogger().warning("Falha ao publicar no Redis: " + t.getMessage());
        }
    }

    public void set(String key, String value, int ttlSeconds) {
        if (!enabled || pool == null || key == null) return;
        try (var jedis = pool.getResource()) {
            if (ttlSeconds > 0) {
                jedis.setex(key, ttlSeconds, value != null ? value : "");
            } else {
                jedis.set(key, value != null ? value : "");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Falha ao gravar Redis key: " + t.getMessage());
        }
    }

    public void subscribe(JedisPubSub handler, String... channels) {
        if (!enabled || pool == null) return;
        Thread t = new Thread(() -> {
            try (var jedis = pool.getResource()) {
                jedis.subscribe(handler, channels);
            } catch (Throwable err) {
                plugin.getLogger().warning("Redis subscribe encerrado: " + err.getMessage());
            }
        }, "eventos-redis-subscriber");
        t.setDaemon(true);
        t.start();
    }

    public void shutdown() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }
}
