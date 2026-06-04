package com.minecraft.lobbyhost.backend.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool de conexoes HikariCP. Suporta MySQL e SQLite (default em dev).
 *
 * <p>O tipo e selecionado em {@code config.yml}.
 */
public final class HikariProvider {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    @Getter
    private DatabaseType type;

    private HikariDataSource dataSource;

    public HikariProvider(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void init() {
        String raw = config.getString("database.type", "MYSQL");
        this.type = DatabaseType.valueOf(raw.toUpperCase());

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("game-pool");

        switch (type) {
            case MYSQL -> {
                String host = config.getString("database.mysql.host", "127.0.0.1");
                int port = config.getInt("database.mysql.port", 3306);
                String db = config.getString("database.mysql.database", "game");
                String user = config.getString("database.mysql.username", "root");
                String pass = config.getString("database.mysql.password", "");
                int pool = config.getInt("database.mysql.pool-size", 10);

                hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                hikari.setUsername(user);
                hikari.setPassword(pass);
                hikari.setMaximumPoolSize(pool);
                hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");

                hikari.addDataSourceProperty("cachePrepStmts", "true");
                hikari.addDataSourceProperty("prepStmtCacheSize", "250");
                hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikari.addDataSourceProperty("useServerPrepStmts", "true");
                hikari.addDataSourceProperty("useLocalSessionState", "true");
                hikari.addDataSourceProperty("rewriteBatchedStatements", "true");
            }
            case SQLITE -> {
                String relative = config.getString("database.sqlite-file", "../../shared/anarquia-dev.db");
                File file = new File(relative);
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    plugin.getLogger().warning("Falha ao criar diretorio para SQLite: " + file.getParent());
                }
                hikari.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
                hikari.setDriverClassName("org.sqlite.JDBC");
                hikari.setMaximumPoolSize(1);
            }
        }

        this.dataSource = new HikariDataSource(hikari);
        plugin.getLogger().info("Banco de dados conectado: " + type);
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("HikariProvider nao inicializado.");
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }
}
