package com.minecraft.game;

import com.minecraft.game.account.AccountController;
import com.minecraft.game.backend.data.AccountData;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.backend.data.AuctionData;
import com.minecraft.game.backend.data.SignShopData;
import com.minecraft.game.bukkit.shop.SignShopConfig;
import com.minecraft.game.bukkit.shop.auction.AuctionManager;
import com.minecraft.game.bukkit.shop.catalog.SellCatalog;
import com.minecraft.game.bukkit.shop.catalog.ServerShopCatalog;
import com.minecraft.game.bukkit.menu.listener.MenuListener;
import com.minecraft.game.backend.database.GameDatabaseSchema;
import com.minecraft.game.backend.database.HikariProvider;
import com.minecraft.game.config.DiscordConfig;
import com.minecraft.game.Constant;
import com.minecraft.game.backend.redis.RedisProvider;
import com.minecraft.game.redis.AccountRedisListener;
import com.minecraft.game.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.game.bukkit.display.AboveNameDisplay;
import com.minecraft.game.bukkit.listener.handler.ListenerHandler;
import com.minecraft.game.bukkit.kit.KitManager;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
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
                redis.subscribe(new AccountRedisListener(this),
                        Constant.REDIS_ACCOUNT_UPDATE_CHANNEL,
                        Constant.REDIS_NETWORK_ALERT_CHANNEL,
                        Constant.REDIS_PROXY_STAFFCHAT_CHANNEL,
                        Constant.REDIS_NETWORK_REPORT_CHANNEL,
                        Constant.REDIS_ANTICHEAT_ALERT_CHANNEL,
                        Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL);
            }

            // 3) Data layers + schema
            AccountData accountData = new AccountData(database);
            ClanData clanData = new ClanData(database);
            com.minecraft.game.backend.data.NetworkStatsData networkStatsData =
                    new com.minecraft.game.backend.data.NetworkStatsData(database);
            com.minecraft.game.backend.data.ReportData reportData =
                    new com.minecraft.game.backend.data.ReportData(database);
            com.minecraft.game.backend.data.PunishmentData punishmentData =
                    new com.minecraft.game.backend.data.PunishmentData(database);
            SignShopData signShopData = new SignShopData(database);
            AuctionData auctionData = new AuctionData(database);

            GameDatabaseSchema.createAll(
                    database,
                    accountData,
                    clanData,
                    networkStatsData,
                    reportData,
                    punishmentData,
                    signShopData,
                    auctionData);

            Core.setAccountData(accountData);
            Core.setClanData(clanData);
            Core.setNetworkStatsData(networkStatsData);
            Core.setReportData(reportData);
            Core.setPunishmentData(punishmentData);
            signShopData.loadAll();
            Core.setSignShopData(signShopData);
            SignShopConfig.load(this);
            auctionData.loadAll();
            Core.setAuctionData(auctionData);
            AuctionManager.configure(
                    GameSettings.AUCTION_MAX_LISTINGS,
                    GameSettings.AUCTION_DURATION_HOURS,
                    GameSettings.AUCTION_TAX_PERCENT,
                    GameSettings.AUCTION_LISTING_FEE);

            ServerShopCatalog.load(this);
            SellCatalog.load(this);
            if (!new java.io.File(getDataFolder(), "physical-shop.yml").exists()) {
                saveResource("physical-shop.yml", false);
            }

            // 4) Controllers
            Core.setAccountController(new AccountController());

            // 5) Task manager (save periodico)
            taskManager = new TaskManager(this);
            taskManager.start();

            SemiAnarchyConfig.load(this);
            ensureSpawnWorldLoaded();
            KitManager.load(this);
            AnticheatConfig.load(this);
            com.minecraft.game.bukkit.vanish.VanishService.start(this);
            CombatTagManager.startActionBarTask(this);
            com.minecraft.game.bukkit.listener.ActionBarListener.start(this);
            AnticheatEngine.start(this);

            TagManager.startAnimationTask(this);
            AboveNameDisplay.init(this);

            // 6.5) Task de limpeza de requests de teleporte expirados
            getServer().getScheduler().runTaskTimer(this,
                    com.minecraft.game.bukkit.manager.TeleportManager::cleanupExpiredRequests,
                    200L, 200L);

            // 7) Scan automatico (igual Savage)
            getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
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
        com.minecraft.game.bukkit.vanish.VanishService.shutdown();
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
        DiscordConfig.load(this);
        SemiAnarchyConfig.reload(this);
        SignShopConfig.load(this);
        if (Core.getSignShopData() != null) Core.getSignShopData().loadAll();
        if (Core.getAuctionData() != null) Core.getAuctionData().loadAll();
        ServerShopCatalog.load(this);
        SellCatalog.load(this);
        KitManager.load(this);
        AnticheatConfig.load(this);
        com.minecraft.game.bukkit.vanish.VanishService.reload(this);
        getLogger().info("Configuracoes, kits, anticheat, vanish e semi-anarquia recarregados.");
    }

    private void ensureSpawnWorldLoaded() {
        String spawnWorld = GameSettings.SPAWN_WORLD;
        if (com.minecraft.game.bukkit.world.WorldManager.isLoaded(spawnWorld)) {
            SemiAnarchyConfig.onWorldLoaded(spawnWorld);
            return;
        }
        if (!com.minecraft.game.bukkit.world.WorldManager.existsOnDisk(spawnWorld)) {
            getLogger().warning("[Mundo] Pasta do mapa spawn nao encontrada (sem level.dat). "
                    + "Coloque o mapa em ./" + spawnWorld + " ou ajuste GameSettings.SPAWN_WORLD.");
            return;
        }
        var result = com.minecraft.game.bukkit.world.WorldManager.load(spawnWorld);
        if (result.success()) {
            getLogger().info("[Mundo] Mapa spawn carregado automaticamente.");
        } else {
            getLogger().warning("[Mundo] Falha ao carregar mapa spawn: " + result.message());
        }
    }
}
