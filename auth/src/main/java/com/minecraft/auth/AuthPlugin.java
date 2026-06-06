package com.minecraft.auth;

import com.minecraft.auth.account.AccountController;
import com.minecraft.auth.backend.data.AccountIdentityData;
import com.minecraft.auth.backend.data.AccountData;
import com.minecraft.auth.backend.database.HikariProvider;
import com.minecraft.auth.backend.redis.RedisProvider;
import com.minecraft.auth.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.auth.bukkit.listener.handler.ListenerHandler;
import com.minecraft.auth.bukkit.manager.TaskManager;
import com.minecraft.auth.redis.AccountRedisListener;
import com.minecraft.auth.config.AuthConfig;
import com.minecraft.auth.message.AuthMessages;
import com.minecraft.auth.validation.ValidationWebServer;
import com.minecraft.auth.AuthSettings;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Plugin {@code auth.jar} - servidor de autenticacao Paper 1.21.
 *
 * <p>Porting de {@code auth/.../Auth.java} do Savage:
 * <ul>
 *   <li>Carrega {@link com.minecraft.auth.account.Account} no pre-login</li>
 *   <li>Bloqueia tudo ate o jogador rodar {@code /login} ou {@code /register}</li>
 *   <li>Redireciona ao hub pos-autenticacao (BungeeCord channel)</li>
 *   <li>Opcionalmente expoe {@link ValidationWebServer} HTTP</li>
 * </ul>
 */
@Getter
public final class AuthPlugin extends JavaPlugin {

    @Getter
    private static AuthPlugin instance;

    private TaskManager taskManager;
    private ValidationWebServer validationServer;

    @Override
    public void onLoad() {
        instance = this;
        Core.load(this);
        getLogger().info("======================================");
        getLogger().info(" auth.jar - login/registro Paper 1.21");
        getLogger().info("======================================");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        long start = System.currentTimeMillis();

        try {
            AuthConfig.load(this);
            AuthMessages.load(this);
            com.minecraft.auth.config.DiscordConfig.load(this);
            FileConfiguration config = getConfig();

            // 1) Banco de dados
            HikariProvider database = new HikariProvider(this, config);
            database.init();
            Core.setDatabase(database);

            // 2) Redis (opcional)
            RedisProvider redis = new RedisProvider(this, config);
            redis.init();
            Core.setRedis(redis);
            if (redis.isEnabled()) {
                redis.subscribe(new AccountRedisListener(this),
                        Constant.REDIS_ACCOUNT_UPDATE_CHANNEL,
                        Constant.REDIS_NETWORK_ALERT_CHANNEL);
            }

            // 3) Data layers
            AccountIdentityData accountIdentityData = new AccountIdentityData(database);
            accountIdentityData.createTables();
            AccountData accountData = new AccountData(database);
            accountData.createTables();
            Core.setAccountIdentityData(accountIdentityData);
            Core.setAccountData(accountData);

            com.minecraft.auth.backend.data.AuthData authData = new com.minecraft.auth.backend.data.AuthData(database);
            authData.createTables();
            Core.setAuthData(authData);
            Core.getLogger().info("Tabela player_auth pronta (" + authData.countRegistered() + " registros).");

            // 4) Controllers
            Core.setAccountController(new AccountController());

            // 5) Task manager
            taskManager = new TaskManager(this);
            taskManager.start();

            // 6) Canal BungeeCord (para /login redirect ao hub)
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

            // 7) Scan automatico
            new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.command");
            new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.listener");

            getServer().getScheduler().runTask(this,
                    () -> com.minecraft.auth.bukkit.command.vanilla.AllowedCommandIndex.rebuild(this));

            // 8) Servidor HTTP de validacao (opcional)
            int port = AuthSettings.VALIDATION_HTTP_PORT;
            if (port > 0) {
                validationServer = new ValidationWebServer(port);
                try {
                    validationServer.start();
                } catch (Throwable t) {
                    getLogger().warning("Nao foi possivel iniciar ValidationWebServer: " + t.getMessage());
                }
            }

            // 9) Reload das contas online
            CompletableFuture.runAsync(() -> getServer().getOnlinePlayers().forEach(p ->
                    Core.getAccountController().load(p.getUniqueId(), p.getName())));

            getLogger().info("Plugin auth habilitado em " + (System.currentTimeMillis() - start) + "ms.");
        } catch (Throwable t) {
            getLogger().severe("Falha critica ao inicializar o plugin auth:");
            t.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (taskManager != null) taskManager.stop();
        if (validationServer != null) validationServer.stop();

        if (Core.getAccountController() != null) {
            getLogger().info("Salvando contas online...");
            Core.getAccountController().saveAllSync();
        }

        Core.unload();
        getLogger().info("Plugin auth desabilitado.");
    }

    public void reloadAll() {
        reloadConfig();
        AuthConfig.load(this);
        AuthMessages.reload(this);
        getLogger().info("Configurações e mensagens recarregadas.");
    }
}
