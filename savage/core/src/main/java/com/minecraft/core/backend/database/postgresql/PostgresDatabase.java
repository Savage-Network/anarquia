package com.minecraft.core.backend.database.postgresql;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.backend.database.Database;
import com.minecraft.core.backend.database.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

@Getter
public class PostgresDatabase implements Database {

    private final DatabaseCredentials credentials;
    private HikariDataSource dataSource;

    public PostgresDatabase(DatabaseCredentials credentials) {
        this.credentials = credentials;
    }

    public PostgresDatabase(boolean local) {
        this(local
                ? DatabaseCredentials.builder()
                .host("198.1.195.80")
                .port(5432)
                .database("savagemc")
                .user("savagemc")
                .password("savagemcnaochorax")
                .build()
                : DatabaseCredentials.builder()
                .host("198.1.195.80")
                .user("savagemc")
                .password("savagemcnaochorax")
                .port(5432)
                .database(Constant.DATABASE_NAME)
                .build());
    }

    @Override
    public void load() {
        Instant now = Instant.now();
        Core.getLogger().info("Conectando ao PostgreSQL...");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            return;
        }

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                HikariConfig config = getHikariConfig();

                dataSource = new HikariDataSource(config);
                
                try (Connection conn = dataSource.getConnection()) {
                    if (conn.isValid(5)) {
                        Core.getLogger().info("Conexão com PostgreSQL efetuada com sucesso. (Tempo médio: " + Duration.between(now, Instant.now()).toMillis() + "ms)");
                        Core.getLogger().info("As tabelas serão criadas automaticamente pelas classes de dados.");
                        break;
                    }
                }
                
            } catch (Exception e) {
                Core.getLogger().warning("Erro ao estabelecer conexão com o PostgreSQL. Tentativa " + (i + 1) + " de " + maxRetries);
                e.printStackTrace();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (dataSource == null || dataSource.isClosed()) {
            Core.getLogger().severe("Não foi possível efetuar a conexão com o PostgreSQL após " + maxRetries +
                " tentativas. Verifique as credenciais.");
        }
    }

    private HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = String.format(
            "jdbc:postgresql://%s:%d/%s",
            credentials.getHost(),
            credentials.getPort(),
            credentials.getDatabase()
        );

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(credentials.getUser());
        config.setPassword(credentials.getPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("HishPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        return config;
    }

    @Override
    public void unload() {
        if (isAvailable()) {
            dataSource.close();
            Core.getLogger().info("Conexão com PostgreSQL encerrada.");
        }
    }

    @Override
    public boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    public Connection getConnection() throws SQLException {
        if (!isAvailable()) {
            throw new SQLException("DataSource não está disponível");
        }
        return dataSource.getConnection();
    }
}
