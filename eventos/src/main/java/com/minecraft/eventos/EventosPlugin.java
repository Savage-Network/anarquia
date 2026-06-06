package com.minecraft.eventos;

import com.minecraft.eventos.account.AccountController;
import com.minecraft.eventos.backend.data.AccountIdentityData;
import com.minecraft.eventos.backend.data.AccountData;
import com.minecraft.eventos.backend.database.HikariProvider;
import com.minecraft.eventos.backend.redis.RedisProvider;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.eventos.bukkit.display.AboveNameDisplay;
import com.minecraft.eventos.bukkit.listener.handler.ListenerHandler;
import com.minecraft.eventos.bukkit.manager.TagManager;
import com.minecraft.eventos.config.EventConfig;
import com.minecraft.eventos.engine.EventEngine;
import com.minecraft.eventos.game.Game;
import com.minecraft.eventos.game.skit.SkitController;
import com.minecraft.eventos.message.EventMessages;
import com.minecraft.eventos.redis.AccountRedisListener;
import com.minecraft.eventos.bukkit.manager.TaskManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

@Getter
public final class EventosPlugin extends JavaPlugin {

    @Getter
    private static EventosPlugin instance;

    private TaskManager taskManager;
    private EventEngine eventEngine;
    private SkitController skitController;

    /** Compat legado */
    public Game getGame() {
        return eventEngine != null ? eventEngine.getSession() : null;
    }

    @Override
    public void onLoad() {
        instance = this;
        Core.load(this);
        getLogger().info("======================================");
        getLogger().info(" eventos.jar - eventos staff + automaticos");
        getLogger().info("======================================");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        long start = System.currentTimeMillis();

        try {
            EventMessages.load(this);
            EventConfig.load(this);
            com.minecraft.eventos.config.DiscordConfig.load(this);
            FileConfiguration config = getConfig();

            HikariProvider database = new HikariProvider(this, config);
            database.init();
            Core.setDatabase(database);

            RedisProvider redis = new RedisProvider(this, config);
            redis.init();
            Core.setRedis(redis);
            if (redis.isEnabled()) {
                redis.subscribe(new AccountRedisListener(this),
                        Constant.REDIS_ACCOUNT_UPDATE_CHANNEL,
                        Constant.REDIS_NETWORK_ALERT_CHANNEL);
            }

            AccountIdentityData accountIdentityData = new AccountIdentityData(database);
            accountIdentityData.createTables();
            AccountData accountData = new AccountData(database);
            accountData.createTables();
            Core.setAccountIdentityData(accountIdentityData);
            Core.setAccountData(accountData);

            Core.setAccountController(new AccountController());

            eventEngine = new EventEngine(this);
            eventEngine.init();
            skitController = new SkitController();

            taskManager = new TaskManager(this);
            taskManager.start();
            startGamePulse();

            TagManager.startAnimationTask(this);
            AboveNameDisplay.startFollowTask(this);

            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

            new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.command");
            new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.listener");

            getServer().getScheduler().runTask(this,
                    () -> com.minecraft.eventos.bukkit.command.vanilla.AllowedCommandIndex.rebuild(this));

            CompletableFuture.runAsync(() -> getServer().getOnlinePlayers().forEach(p ->
                    Core.getAccountController().load(p.getUniqueId(), p.getName())));

            getLogger().info("Plugin eventos habilitado em "
                    + (System.currentTimeMillis() - start) + "ms.");
        } catch (Throwable t) {
            getLogger().severe("Falha critica ao inicializar o plugin eventos:");
            t.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        AboveNameDisplay.shutdown();
        TagManager.stopAnimationTask();
        if (eventEngine != null) eventEngine.shutdown();
        if (skitController != null) skitController.clear();
        if (taskManager != null) taskManager.stop();

        if (Core.getAccountController() != null) {
            getLogger().info("Salvando contas online...");
            Core.getAccountController().saveAllSync();
        }

        Core.unload();
        getLogger().info("Plugin eventos desabilitado.");
    }

    private void startGamePulse() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (eventEngine != null) eventEngine.pulse();
        }, 20L, 20L);
    }

    public void reloadAll() {
        reloadConfig();
        EventConfig.load(this);
        EventMessages.reload(this);
        getLogger().info("Configuracoes e mensagens recarregadas.");
    }
}
