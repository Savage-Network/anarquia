package com.minecraft.game.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraft.game.account.context.AccountContext;
import com.minecraft.game.account.context.objects.rank.Rank;
import com.minecraft.game.account.context.objects.rank.RankInfo;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * CLI de dev: define rank de um jogador na tabela {@code accounts} (MySQL ou SQLite).
 *
 * <p>Uso:
 * <pre>
 *   java ... SetPlayerRankTool --config dev/secrets/database.yml zrwfaWASD OWNER
 * </pre>
 */
public final class SetPlayerRankTool {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private SetPlayerRankTool() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso:");
            System.err.println("  SetPlayerRankTool [--config <database.yml>] <nick> <RANK>");
            System.err.println("  SetPlayerRankTool --migrate-to-mysql [--config <database.yml>]");
            System.exit(1);
        }

        if (args[0].equals("--migrate-to-mysql")) {
            Path cfg = args.length > 1 && args[1].equals("--config")
                    ? Path.of(args[2])
                    : Path.of("dev/secrets/database.yml");
            migrateSqliteToMysql(cfg);
            return;
        }

        if (args.length < 2) {
            System.err.println("Faltam argumentos: <nick> <RANK>");
            System.exit(1);
        }

        Path configPath = null;
        int argStart = 0;
        if (args[0].equals("--config")) {
            if (args.length < 4) {
                System.err.println("Falta caminho apos --config");
                System.exit(1);
            }
            configPath = Path.of(args[1]);
            argStart = 2;
        }

        String nick = args[argStart];
        RankType rankType = RankType.valueOf(args[argStart + 1].toUpperCase(Locale.ROOT));

        DbConfig db = configPath != null
                ? DbConfig.fromYaml(configPath)
                : DbConfig.sqlite(Path.of("dev/runtime/shared/anarquia-dev.db"));

        UUID id = offlineUuid(nick);
        System.out.println("Jogador: " + nick);
        System.out.println("UUID (offline): " + id);
        System.out.println("Rank: " + rankType.name());
        System.out.println("Banco: " + db.type + " -> " + db.jdbcUrl);

        try (HikariDataSource ds = db.dataSource()) {
            ensureSchema(ds, db.type);
            upsertAccount(ds, db.type, id, nick, rankType);
        }

        System.out.println("Conta salva com sucesso.");
    }

    private static void migrateSqliteToMysql(Path mysqlConfig) throws Exception {
        DbConfig mysql = DbConfig.fromYaml(mysqlConfig);
        if (!"MYSQL".equals(mysql.type)) {
            System.err.println("database.yml deve ser type: MYSQL");
            System.exit(1);
        }
        DbConfig sqlite = DbConfig.sqlite(Path.of("dev/runtime/shared/anarquia-dev.db"));
        System.out.println("Migrando accounts SQLite -> MySQL...");

        try (HikariDataSource src = sqlite.dataSource(); HikariDataSource dst = mysql.dataSource()) {
            ensureMysqlDatabase(mysql);
            ensureSchema(dst, "MYSQL");
            int count = 0;
            try (Connection c = src.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT id, name, data FROM accounts");
                 ResultSet rs = ps.executeQuery()) {
                String upsert = """
                        INSERT INTO accounts (id, name, data) VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE name = VALUES(name), data = VALUES(data),
                        updated_at = CURRENT_TIMESTAMP""";
                try (Connection target = dst.getConnection();
                     PreparedStatement ins = target.prepareStatement(upsert)) {
                    while (rs.next()) {
                        ins.setString(1, rs.getString("id"));
                        ins.setString(2, rs.getString("name"));
                        ins.setString(3, rs.getString("data"));
                        ins.executeUpdate();
                        count++;
                    }
                }
            }
            System.out.println("Migradas " + count + " contas para MySQL.");
        }
    }

    private static void ensureMysqlDatabase(DbConfig mysql) {
        String baseUrl = mysql.jdbcUrl.replaceAll("/[^/?]+\\?", "/?");
        if (!baseUrl.contains("?")) {
            int slash = mysql.jdbcUrl.indexOf('/', "jdbc:mysql://".length());
            baseUrl = mysql.jdbcUrl.substring(0, slash) + "/";
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(baseUrl);
        cfg.setUsername(mysql.user);
        cfg.setPassword(mysql.pass);
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        try (HikariDataSource ds = new HikariDataSource(cfg);
             Connection c = ds.getConnection();
             Statement st = c.createStatement()) {
            String db = mysql.jdbcUrl.replaceAll(".*\\/([^/?]+)(\\?.*)?$", "$1");
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + db + "` CHARACTER SET utf8mb4");
        } catch (SQLException e) {
            System.out.println("[AVISO] CREATE DATABASE ignorado (hosting remoto): " + e.getMessage());
        }
    }

    private static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private static void ensureSchema(HikariDataSource ds, String type) throws SQLException {
        String jsonCol = "MYSQL".equals(type) ? "JSON" : "TEXT";
        String sql = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id          VARCHAR(36) PRIMARY KEY,
                    name        VARCHAR(16) NOT NULL,
                    data        %s NOT NULL,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""".formatted(jsonCol);

        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        }

        String clans = """
                CREATE TABLE IF NOT EXISTS clans (
                    id          VARCHAR(36) PRIMARY KEY,
                    name        VARCHAR(32) NOT NULL,
                    tag         VARCHAR(16) NOT NULL,
                    data        %s NOT NULL,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""".formatted(jsonCol);
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate(clans);
        }
    }

    private static void upsertAccount(HikariDataSource ds, String type, UUID id, String nick, RankType rankType)
            throws SQLException {
        AccountContext ctx = loadContext(ds, id).orElse(new AccountContext());

        Rank rank = Rank.builder().type(rankType).givenAt(System.currentTimeMillis()).expiresAt(0L).build();
        RankInfo info = ctx.getRankInfo() != null ? ctx.getRankInfo() : new RankInfo();
        info.setRank(rank);
        ctx.setRankInfo(info);
        ctx.setTag(Tag.of(rankType));

        String json = GSON.toJson(ctx);
        String upsert = "MYSQL".equals(type)
                ? """
                INSERT INTO accounts (id, name, data) VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE name = VALUES(name), data = VALUES(data),
                updated_at = CURRENT_TIMESTAMP"""
                : """
                INSERT INTO accounts (id, name, data) VALUES (?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET name = excluded.name, data = excluded.data,
                updated_at = CURRENT_TIMESTAMP""";

        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, id.toString());
            ps.setString(2, nick);
            ps.setString(3, json);
            ps.executeUpdate();
        }
    }

    private static Optional<AccountContext> loadContext(HikariDataSource ds, UUID id) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT data FROM accounts WHERE id = ?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                AccountContext ctx = GSON.fromJson(rs.getString("data"), AccountContext.class);
                return Optional.ofNullable(ctx);
            }
        }
    }

    private record DbConfig(String type, String jdbcUrl, String user, String pass) {

        HikariDataSource dataSource() {
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(jdbcUrl);
            if ("MYSQL".equals(type)) {
                cfg.setUsername(user);
                cfg.setPassword(pass);
                cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
                cfg.setMaximumPoolSize(5);
            } else {
                cfg.setDriverClassName("org.sqlite.JDBC");
                cfg.setMaximumPoolSize(1);
            }
            return new HikariDataSource(cfg);
        }

        static DbConfig sqlite(Path file) {
            return new DbConfig("SQLITE", "jdbc:sqlite:" + file.toAbsolutePath(), null, null);
        }

        static DbConfig fromYaml(Path path) throws IOException {
            String type = "SQLITE";
            String host = "127.0.0.1";
            int port = 3306;
            String database = "anarquia";
            String user = "root";
            String pass = "";

            for (String line : Files.readAllLines(path)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                if (t.startsWith("type:")) type = t.substring(5).trim().toUpperCase(Locale.ROOT);
                if (t.startsWith("host:")) host = t.substring(5).trim();
                if (t.startsWith("port:")) port = Integer.parseInt(t.substring(5).trim());
                if (t.startsWith("database:")) database = t.substring(9).trim();
                if (t.startsWith("username:")) user = t.substring(9).trim();
                if (t.startsWith("password:")) pass = t.substring(9).trim();
            }

            if ("MYSQL".equals(type)) {
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                return new DbConfig("MYSQL", url, user, pass);
            }
            return sqlite(Path.of("dev/runtime/shared/anarquia-dev.db"));
        }
    }
}
