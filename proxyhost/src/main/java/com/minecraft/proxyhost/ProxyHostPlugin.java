package com.minecraft.proxyhost;

import com.google.inject.Inject;
import com.minecraft.proxyhost.account.AccountController;
import com.minecraft.proxyhost.backend.data.AccountData;
import com.minecraft.proxyhost.backend.database.HikariProvider;
import com.minecraft.proxyhost.backend.redis.RedisProvider;
import com.minecraft.proxyhost.config.ConfigSection;
import com.minecraft.proxyhost.redis.RedisListener;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.minecraft.proxyhost.velocity.command.AccountCommand;
import com.minecraft.proxyhost.velocity.command.ProxyCommand;
import com.minecraft.proxyhost.velocity.command.ReplyCommand;
import com.minecraft.proxyhost.velocity.command.StaffChatCommand;
import com.minecraft.proxyhost.velocity.command.TellCommand;
import com.minecraft.proxyhost.velocity.listener.LoginListener;
import com.minecraft.proxyhost.velocity.listener.ProxyPingListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Plugin principal do proxy (Velocity 3.3, Paper 1.21 compativel).
 *
 * <p>Compartilha a tabela {@code accounts} com os plugins {@code game}, {@code auth},
 * {@code lobbyhost} e {@code eventos} para manter consistencia de tags, medalhas e ranks.
 */
@Plugin(
        id = "proxyhost",
        name = "ProxyHost",
        version = "1.0.0",
        description = "Proxy host (Savage anarquia) - MOTD, manutencao, /tell, /staffchat, /account.",
        authors = {"savage-anarquia"}
)
public final class ProxyHostPlugin {

    @Getter private final ProxyServer proxy;
    @Getter private final Logger logger;
    @Getter private final Path dataDirectory;

    @Getter private ConfigSection config;

    @Inject
    public ProxyHostPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        long start = System.currentTimeMillis();
        Core.load(this, proxy, logger);

        try {
            this.config = loadConfig();
        } catch (Exception e) {
            logger.error("Falha ao carregar config.yml: " + e.getMessage(), e);
            return;
        }

        HikariProvider database = new HikariProvider(config, dataDirectory);
        try {
            database.init();
        } catch (Exception mysqlError) {
            boolean devMode = config.getBoolean("dev.enabled", false);
            if (devMode && !"SQLITE".equalsIgnoreCase(config.getString("database.type", "MYSQL"))) {
                logger.warn("MySQL indisponivel ({}). Tentando SQLite de dev...", mysqlError.getMessage());
                try {
                    database.initDevSqliteFallback();
                } catch (Exception sqliteError) {
                    logger.error("Falha MySQL e SQLite. MySQL: {}", mysqlError.getMessage());
                    logger.error("SQLite: {}", sqliteError.getMessage(), sqliteError);
                    return;
                }
            } else {
                logger.error("Falha ao conectar banco de dados: {}", mysqlError.getMessage(), mysqlError);
                return;
            }
        }
        Core.setDatabase(database);

        RedisProvider redis = new RedisProvider(config);
        redis.init();
        Core.setRedis(redis);

        AccountData accountData = new AccountData(database);
        accountData.createTables();
        Core.setAccountData(accountData);

        AccountController controller = new AccountController();
        Core.setAccountController(controller);

        ProxySettingsManager proxySettings = new ProxySettingsManager(dataDirectory);
        proxySettings.load();

        // Listeners
        proxy.getEventManager().register(this, new LoginListener());
        proxy.getEventManager().register(this, new ProxyPingListener());

        // Comandos
        var cm = proxy.getCommandManager();
        cm.register(cm.metaBuilder("account").aliases("a").plugin(this).build(), new AccountCommand());
        cm.register(cm.metaBuilder("tell").aliases("msg", "w", "pm").plugin(this).build(), new TellCommand());
        cm.register(cm.metaBuilder("reply").aliases("r").plugin(this).build(), new ReplyCommand());
        cm.register(cm.metaBuilder("staffchat").aliases("sc").plugin(this).build(), new StaffChatCommand());
        cm.register(cm.metaBuilder("proxy").aliases("proxyadmin", "velocity").plugin(this).build(), new ProxyCommand());

        // Redis subscriber - tell e staffchat cross-proxy/cross-server
        if (redis.isEnabled()) {
            redis.subscribe(new RedisListener(),
                    Constant.REDIS_PROXY_TELL_CHANNEL,
                    Constant.REDIS_PROXY_STAFFCHAT_CHANNEL,
                    Constant.REDIS_ACCOUNT_UPDATE_CHANNEL,
                    Constant.REDIS_PROXY_SETTINGS_CHANNEL);
        }

        long took = System.currentTimeMillis() - start;
        logger.info("proxyhost habilitado em {}ms.", took);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        if (ProxySettingsManager.get() != null) {
            ProxySettingsManager.get().save();
        }
        Core.unload();
        logger.info("proxyhost desabilitado.");
    }

    @SuppressWarnings("unchecked")
    private ConfigSection loadConfig() throws Exception {
        if (!Files.exists(dataDirectory)) Files.createDirectories(dataDirectory);

        Path target = dataDirectory.resolve("config.yml");
        if (!Files.exists(target)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in == null) throw new IllegalStateException("config.yml default ausente do jar.");
                Files.copy(in, target);
            }
        }
        try (InputStream in = Files.newInputStream(target)) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(in);
            return new ConfigSection(root == null ? Map.of() : root);
        }
    }
}
