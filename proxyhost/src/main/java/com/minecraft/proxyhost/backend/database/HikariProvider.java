package com.minecraft.proxyhost.backend.database;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.config.ConfigSection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool HikariCP - suporta MySQL e SQLite. Versao Velocity (sem dependencia de Bukkit).
 */
public final class HikariProvider {

    private final ConfigSection config;
    private final Path dataDirectory;

    @Getter
    private DatabaseType type;

    private HikariDataSource dataSource;

    public HikariProvider(ConfigSection config, Path dataDirectory) {
        this.config = config;
        this.dataDirectory = dataDirectory;
    }

    public void init() {
        String raw = config.getString("database.type", "SQLITE");
        this.type = DatabaseType.valueOf(raw.toUpperCase());

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("proxyhost-pool");

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
                String relative = config.getString("database.sqlite-file", "proxyhost.db");
                File file = dataDirectory.resolve(relative).toFile();
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                hikari.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
                hikari.setDriverClassName("org.sqlite.JDBC");
                hikari.setMaximumPoolSize(1);
            }
        }

        this.dataSource = new HikariDataSource(hikari);
        Core.getLogger().info("Banco de dados conectado: " + type);
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

    public enum DatabaseType { MYSQL, SQLITE }
}
