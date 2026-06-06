package com.minecraft.lobbyhost;

import com.minecraft.lobbyhost.account.AccountController;
import com.minecraft.lobbyhost.architect.ArchitectLoader;
import com.minecraft.lobbyhost.backend.data.AccountIdentityData;
import com.minecraft.lobbyhost.backend.data.AccountData;
import com.minecraft.lobbyhost.backend.data.ClanData;
import com.minecraft.lobbyhost.backend.data.NetworkStatsData;
import com.minecraft.lobbyhost.backend.sync.StatsBackfillService;
import com.minecraft.lobbyhost.leaderboard.LeaderboardCache;
import com.minecraft.lobbyhost.bukkit.menu.listener.MenuListener;
import com.minecraft.lobbyhost.backend.database.HikariProvider;
import com.minecraft.lobbyhost.config.DiscordConfig;
import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.backend.redis.RedisProvider;
import com.minecraft.lobbyhost.redis.LobbyRedisSubscriber;
import com.minecraft.lobbyhost.network.NetworkPollTask;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.lobbyhost.bukkit.display.AboveNameDisplay;
import com.minecraft.lobbyhost.bukkit.listener.handler.ListenerHandler;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.manager.TaskManager;
import com.minecraft.lobbyhost.display.DiscordHologramManager;
import com.minecraft.lobbyhost.display.npc.HardcodedNpcs;
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
            DiscordConfig.load(this);
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
                redis.subscribe(new LobbyRedisSubscriber(this), Constant.REDIS_CHANNELS);
                new NetworkPollTask().runTaskTimerAsynchronously(this,
                        LobbySettings.NETWORK_POLL_TICKS, LobbySettings.NETWORK_POLL_TICKS);
            }

            // 3) Data layers
            AccountIdentityData accountIdentityData = new AccountIdentityData(database);
            accountIdentityData.createTables();
            AccountData accountData = new AccountData(database);
            accountData.createTables();
            Core.setAccountIdentityData(accountIdentityData);
            Core.setAccountData(accountData);

            ClanData clanData = new ClanData(database);
            clanData.createTables();
            Core.setClanData(clanData);

            NetworkStatsData networkStatsData = new NetworkStatsData(database);
            networkStatsData.createTables();
            Core.setNetworkStatsData(networkStatsData);

            com.minecraft.lobbyhost.backend.data.ReportData reportData =
                    new com.minecraft.lobbyhost.backend.data.ReportData(database);
            reportData.createTables();
            Core.setReportData(reportData);

            com.minecraft.lobbyhost.backend.data.PunishmentData punishmentData =
                    new com.minecraft.lobbyhost.backend.data.PunishmentData(database);
            punishmentData.createTables();
            Core.setPunishmentData(punishmentData);

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

            // 6.5) NPCs hardcoded diretamente no código (não dependem de YAML)
            HardcodedNpcs.spawnAll();

            // 7) Tag/medal/chat - Fase 2
            TagManager.startAnimationTask(this);
            AboveNameDisplay.init(this);
            com.minecraft.lobbyhost.bukkit.vanish.VanishService.start(this);
            com.minecraft.lobbyhost.bukkit.manager.CosmeticManager.init();

            // 8) Discord hologram (TextDisplay 1.21)
            DiscordHologramManager.initialize();

            // 8) Canal BungeeCord (para redirect entre servidores)
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getServer().getMessenger().registerOutgoingPluginChannel(this, Constant.STAFFCHAT_PLUGIN_CHANNEL);

            // 9) Scan automatico
            new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.command");
            new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".bukkit.listener");
            getServer().getPluginManager().registerEvents(new MenuListener(), this);

            // 10) Reload de contas dos online
            CompletableFuture.runAsync(() -> getServer().getOnlinePlayers().forEach(p ->
                    Core.getAccountController().load(p.getUniqueId(), p.getName())));

            StatsBackfillService.runAsync();
            LeaderboardCache.refreshAll();

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
        HardcodedNpcs.removeAll();
        NpcManager.unloadAll();
        DiscordHologramManager.shutdown();
        AboveNameDisplay.shutdown();
        com.minecraft.lobbyhost.bukkit.vanish.VanishService.shutdown();
        TagManager.stopAnimationTask();
        com.minecraft.lobbyhost.bukkit.manager.CosmeticManager.shutdown();

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
        DiscordConfig.load(this);
        com.minecraft.lobbyhost.bukkit.vanish.VanishService.reload(this);
        getLogger().info("Configuracoes, discord e vanish recarregados.");
    }
}
