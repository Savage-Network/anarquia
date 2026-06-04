package com.minecraft.eventos;

import com.minecraft.eventos.account.AccountController;
import com.minecraft.eventos.backend.data.AccountData;
import com.minecraft.eventos.backend.database.HikariProvider;
import com.minecraft.eventos.backend.redis.RedisProvider;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.eventos.bukkit.display.AboveNameDisplay;
import com.minecraft.eventos.bukkit.listener.handler.ListenerHandler;
import com.minecraft.eventos.bukkit.manager.TagManager;
import com.minecraft.eventos.bukkit.manager.TaskManager;
import com.minecraft.eventos.game.Game;
import com.minecraft.eventos.game.skit.SkitController;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Plugin {@code eventos.jar} - servidor de eventos staff Paper 1.21.
 *
 * <p>Porting de {@code Eventos/.../Event.java}. Gerencia {@link Game} FSM
 * + {@link SkitController} + comandos staff.
 */
@Getter
public final class EventosPlugin extends JavaPlugin {

    @Getter
    private static EventosPlugin instance;

    private TaskManager taskManager;
    private Game game;
    private SkitController skitController;

    @Override
    public void onLoad() {
        instance = this;
        Core.load(this);
        getLogger().info("======================================");
        getLogger().info(" eventos.jar - staff events Paper 1.21");
        getLogger().info("======================================");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        long start = System.currentTimeMillis();

        try {
            FileConfiguration config = getConfig();

            // 1) Banco
            HikariProvider database = new HikariProvider(this, config);
            database.init();
            Core.setDatabase(database);

            // 2) Redis
            RedisProvider redis = new RedisProvider(this, config);
            redis.init();
            Core.setRedis(redis);

            // 3) Data
            AccountData accountData = new AccountData(database);
            accountData.createTables();
            Core.setAccountData(accountData);

            // 4) Controllers
            Core.setAccountController(new AccountController());

            // 5) Game state machine
            game = new Game();
            skitController = new SkitController();

            // 6) Task manager + pulse
            taskManager = new TaskManager(this);
            taskManager.start();
            startGamePulse();

            // 6.5) Tag/medal/chat - Fase 2
            TagManager.startAnimationTask(this);
            AboveNameDisplay.startFollowTask(this);

            // 7) Canal BungeeCord (para /leave e fim de evento)
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

            // 8) Scan
            new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.command");
            new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.listener");

            // 9) Reload contas online
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
            if (game != null) game.pulse();
        }, 20L, 20L);
    }

    public void reloadAll() {
        reloadConfig();
        getLogger().info("Configuracoes recarregadas.");
    }
}
