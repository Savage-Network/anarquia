package com.minecraft.lobbyhost;

import com.minecraft.lobbyhost.account.AccountController;
import com.minecraft.lobbyhost.architect.ArchitectLoader;
import com.minecraft.lobbyhost.backend.data.AccountData;
import com.minecraft.lobbyhost.backend.database.HikariProvider;
import com.minecraft.lobbyhost.backend.redis.RedisProvider;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.bukkit.listener.handler.ListenerHandler;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.manager.TaskManager;
import com.minecraft.lobbyhost.display.DiscordHologramManager;
import com.minecraft.lobbyhost.display.npc.NpcManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Plugin {@code lobbyhost.jar} - hub + lobbies de modo (Paper 1.21).
 *
 * <p>Espelha {@code lobby/.../Lobby.java}: carrega {@link ArchitectLoader}
 * baseado em {@code architect.json} e instancia o {@link com.minecraft.lobbyhost.architect.Architect}
 * adequado (MainArchitect, BedWarsArchitect, etc.).
 */
@Getter
public final class LobbyHostPlugin extends JavaPlugin {

    @Getter
    private static LobbyHostPlugin instance;

    private TaskManager taskManager;
    private ArchitectLoader architectLoader;

    @Override
    public void onLoad() {
        instance = this;
        Core.load(this);
        getLogger().info("======================================");
        getLogger().info(" lobbyhost.jar - hub Paper 1.21");
        getLogger().info("======================================");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().toPath().resolve("architect.json").toFile().exists()) {
            saveResource("architect.json", false);
        }
        if (!getDataFolder().toPath().resolve("npcs.yml").toFile().exists()) {
            saveResource("npcs.yml", false);
        }
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

            // 4) Controllers
            Core.setAccountController(new AccountController());

            // 5) Task manager (animacoes, save periodico)
            taskManager = new TaskManager(this);
            taskManager.start();

            // 6) Architect (le architect.json + instancia MainArchitect/etc)
            architectLoader = new ArchitectLoader(this);
            NpcManager.load(this);

            architectLoader.load();
            architectLoader.getArchitect().load();

            // 7) Tag/medal/chat - Fase 2
            TagManager.startAnimationTask(this);
            AboveNameDisplay.startFollowTask(this);

            // 8) Discord hologram (TextDisplay 1.21)
            DiscordHologramManager.initialize();

            // 8) Canal BungeeCord (para redirect entre servidores)
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

            // 9) Scan automatico
            new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.command");
            new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.listener");

            // 10) Reload de contas dos online
            CompletableFuture.runAsync(() -> getServer().getOnlinePlayers().forEach(p ->
                    Core.getAccountController().load(p.getUniqueId(), p.getName())));

            getLogger().info("Plugin lobbyhost habilitado em "
                    + (System.currentTimeMillis() - start) + "ms.");
        } catch (Throwable t) {
            getLogger().severe("Falha critica ao inicializar o plugin lobbyhost:");
            t.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        NpcManager.unloadAll();
        DiscordHologramManager.shutdown();
        AboveNameDisplay.shutdown();
        TagManager.stopAnimationTask();

        if (architectLoader != null) architectLoader.unload();
        if (taskManager != null) taskManager.stop();

        if (Core.getAccountController() != null) {
            getLogger().info("Salvando contas online...");
            Core.getAccountController().saveAllSync();
        }

        Core.unload();
        getLogger().info("Plugin lobbyhost desabilitado.");
    }

    public void reloadAll() {
        reloadConfig();
        getLogger().info("Configuracoes recarregadas.");
    }
}
