package com.minecraft.game.backend.database;

import com.minecraft.game.Core;
import com.minecraft.game.backend.data.AccountData;
import com.minecraft.game.backend.data.AuctionData;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.backend.data.NetworkStatsData;
import com.minecraft.game.backend.data.PunishmentData;
import com.minecraft.game.backend.data.ReportData;
import com.minecraft.game.backend.data.SignShopData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Cria todas as tabelas e indices do modulo {@code game} no banco configurado.
 *
 * <p>Scripts SQL equivalentes em {@code db/schema-mysql.sql} e {@code db/schema-sqlite.sql}.
 */
public final class GameDatabaseSchema {

    private GameDatabaseSchema() {
    }

    public static void createAll(
            HikariProvider database,
            AccountData accountData,
            ClanData clanData,
            NetworkStatsData networkStatsData,
            ReportData reportData,
            PunishmentData punishmentData,
            SignShopData signShopData,
            AuctionData auctionData) {

        accountData.createTables();
        clanData.createTables();
        networkStatsData.createTables();
        reportData.createTables();
        punishmentData.createTables();
        signShopData.createTables();
        auctionData.createTables();

        createIndexes(database);

        Core.getLogger().info(
                "Schema do game aplicado: accounts, clans, network_stats, player_reports, "
                        + "punishments, sign_shops, auction_listings");
    }

    /** Indices extras (idempotente via IF NOT EXISTS). */
    public static void createIndexes(HikariProvider database) {
        index(database, "idx_accounts_name", "accounts(name)");
        index(database, "idx_clans_name", "clans(name)");
        index(database, "idx_clans_tag", "clans(tag)");
        index(database, "idx_network_stats_category", "network_stats(category)");
        index(database, "idx_reports_status_created", "player_reports(status, created_at)");
        index(database, "idx_reports_target_created", "player_reports(target_uuid, created_at)");
        index(database, "idx_punishments_player", "punishments(player)");
        index(database, "idx_sign_shops_loc", "sign_shops(world, x, y, z)");
        index(database, "idx_sign_shops_owner", "sign_shops(owner_id)");
        index(database, "idx_auction_seller", "auction_listings(seller_id)");
        index(database, "idx_auction_expires", "auction_listings(expires_at)");
    }

    private static void index(HikariProvider database, String name, String onClause) {
        String sql = "CREATE INDEX IF NOT EXISTS " + name + " ON " + onClause;
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warning("Indice " + name + " nao criado: " + e.getMessage());
        }
    }
}
