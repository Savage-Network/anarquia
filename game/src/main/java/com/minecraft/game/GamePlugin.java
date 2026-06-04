package com.minecraft.game;

import com.minecraft.game.account.AccountController;
import com.minecraft.game.backend.data.AccountData;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.bukkit.menu.listener.MenuListener;
import com.minecraft.game.backend.database.HikariProvider;
import com.minecraft.game.backend.redis.RedisProvider;
import com.minecraft.game.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.game.bukkit.display.AboveNameDisplay;
import com.minecraft.game.bukkit.listener.handler.ListenerHandler;
import com.minecraft.game.bukkit.manager.TagManager;
import com.minecraft.game.bukkit.manager.TaskManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Plugin unico {@code game.jar} - servidor semi-anarquia (Paper 1.21).
 *
 * <p>Espelha o ciclo de vida de {@code com.minecraft.core.bukkit.BukkitCore}
 * do Savage: bootstrap em {@code onLoad}, controllers/scan em {@code onEnable},
 * save/teardown em {@code onDisable}.
 */
@Getter
public final class GamePlugin extends JavaPlugin {

    @Getter
    private static GamePlugin instance;

    private TaskManager taskManager;

    @Override
    public void onLoad() {
        instance = this;
        Core.load(this);
        getLogger().info("======================================");
        getLogger().info(" game.jar - semi-anarquia Paper 1.21");
        getLogger().info("======================================");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        long start = System.currentTimeMillis();

        try {
            FileConfiguration config = getConfig();

            // 1) Banco de dados
            HikariProvider database = new HikariProvider(this, config);
            database.init();
            Core.setDatabase(database);

            // 2) Redis (opcional)
            RedisProvider redis = new RedisProvider(this, config);
            redis.init();
            Core.setRedis(redis);

            // 3) Data layers
            AccountData accountData = new AccountData(database);
            accountData.createTables();
            Core.setAccountData(accountData);

            ClanData clanData = new ClanData(database);
            clanData.createTables();
            Core.setClanData(clanData);

            // 4) Controllers
            Core.setAccountController(new AccountController());

            // 5) Task manager (save periodico)
            taskManager = new TaskManager(this);
            taskManager.start();

            TagManager.startAnimationTask(this);
            AboveNameDisplay.startFollowTask(this);

            // 6.5) Task de limpeza de requests de teleporte expirados
            getServer().getScheduler().runTaskTimer(this,
                    com.minecraft.game.bukkit.manager.TeleportManager::cleanupExpiredRequests,
                    200L, 200L);

            // 7) Scan automatico (igual Savage)
            new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.command");
            new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.listener");
            getServer().getPluginManager().registerEvents(new MenuListener(), this);

            // 7) Carregar contas dos jogadores ja online (reload)
            CompletableFuture.runAsync(() -> getServer().getOnlinePlayers().forEach(p ->
                    Core.getAccountController().load(p.getUniqueId(), p.getName())));

            getLogger().info("Plugin habilitado em " + (System.currentTimeMillis() - start) + "ms.");
        } catch (Throwable t) {
            getLogger().severe("Falha critica ao inicializar o plugin:");
            t.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        AboveNameDisplay.shutdown();
        TagManager.stopAnimationTask();
        if (taskManager != null) taskManager.stop();

        if (Core.getAccountController() != null) {
            getLogger().info("Salvando contas online...");
            Core.getAccountController().saveAllSync();
        }

        Core.unload();
        getLogger().info("Plugin desabilitado.");
    }

    public void reloadAll() {
        reloadConfig();
        getLogger().info("Configuracoes recarregadas.");
    }
}
