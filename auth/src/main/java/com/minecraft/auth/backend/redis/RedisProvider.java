package com.minecraft.auth.backend.redis;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
        int database = config.getInt("redis.database", 0);
        boolean ssl = config.getBoolean("redis.ssl", false);
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

    public void shutdown() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }
}
