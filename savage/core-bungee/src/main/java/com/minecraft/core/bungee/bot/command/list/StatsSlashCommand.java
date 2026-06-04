package com.minecraft.core.bungee.bot.command.list;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.core.bungee.bot.command.structure.category.SlashCommandCategory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatsSlashCommand extends SlashCommand {

    private static final String NICK_KEY = "nick";
    private static final String MODE_KEY = "modo";

    public StatsSlashCommand() {
        super("stats", "Veja as estatísticas de BedWars de um jogador.", SlashCommandCategory.SERVER);

        getOptions().add(new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.STRING)
                .setName(NICK_KEY)
                .setDescription("Nick do jogador.")
                .setRequired(true)
                .build());

        getOptions().add(new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.STRING)
                .setName(MODE_KEY)
                .setDescription("Modo do BedWars.")
                .setRequired(true)
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("Geral").setValue("BEDWARS_GENERAL").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars Solo").setValue("BEDWARS_SOLO").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars Duplas").setValue("BEDWARS_DUO").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars Trios").setValue("BEDWARS_TRIO").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars Quartetos").setValue("BEDWARS_QUARTET").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars 1v1").setValue("BEDWARS_VERSUS_SOLO").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars 2v2").setValue("BEDWARS_VERSUS_DUO").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars 3v3").setValue("BEDWARS_VERSUS_TRIO").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars 4v4").setValue("BEDWARS_VERSUS_QUARTET").build())
                .addChoice(new SlashCommandOptionChoiceBuilder().setName("BedWars Infection").setValue("BEDWARS_INFECTION").build())
                .build());
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        var responder = interaction.respondLater().join();

        String nick = interaction.getArgumentStringValueByName(NICK_KEY).orElse(null);
        String modeStr = interaction.getArgumentStringValueByName(MODE_KEY).orElse(null);

        if (nick == null || modeStr == null) {
            responder.setContent("❌ Parâmetros inválidos.").update().join();
            return;
        }

        Account account = Core.getAccountData().of(nick);
        if (account == null) {
            responder.setContent("❌ Jogador **" + nick + "** não encontrado.").update().join();
            return;
        }

        // Caso especial: BEDWARS_GENERAL soma todos os modos
        if (modeStr.equals("BEDWARS_GENERAL")) {
            JsonObject combinedStats = fetchCombinedBedStats(account.getId().toString());
            if (combinedStats == null) {
                responder.setContent("❌ **" + nick + "** não possui dados de BedWars.").update().join();
                return;
            }

            int matches    = getInt(combinedStats, "matches");
            int wins       = getInt(combinedStats, "wins");
            int defeats    = getInt(combinedStats, "defeats");
            int kills      = getInt(combinedStats, "kills");
            int deaths     = getInt(combinedStats, "deaths");
            int finalKills = getInt(combinedStats, "finalKills");
            int beds       = getInt(combinedStats, "bedDestruction");
            int bedsLost   = matches - wins;
            int winStreak      = getInt(combinedStats, "winStreak");
            int highWinStreak  = getInt(combinedStats, "highWinStreak");
            int level      = getInt(combinedStats, "level");
            int xp         = getInt(combinedStats, "xp");
            int rankedPoints = getInt(combinedStats, "rankedPoints");

            String winPct = matches == 0 ? "0%" : (Math.round((double) wins / matches * 1000.0) / 10.0) + "%";
            double kdr  = deaths   == 0 ? kills : Math.round((double) kills / deaths   * 100.0) / 100.0;
            double wlr  = defeats  == 0 ? wins  : Math.round((double) wins  / defeats  * 100.0) / 100.0;
            double bblr = bedsLost == 0 ? beds  : Math.round((double) beds  / bedsLost * 100.0) / 100.0;

            String modeName  = "BedWars Geral";
            String avatarUrl = String.format(Constant.MINOTAR_AVATAR_API_URL, account.getName(), 120);
            
            // Obter rank baseado nos pontos
            String rankedTier = getRankedTierName(rankedPoints);

            String description =
                    "**Nível:** " + level + "\n" +
                    "**XP:** " + xp + "\n\n" +
                    "**Partidas:** " + matches + "\n" +
                    "**Vitórias:** " + wins + " (" + winPct + ")\n" +
                    "**Derrotas:** " + defeats + "\n\n" +
                    "**Kills:** " + kills + "\n" +
                    "**Deaths:** " + deaths + "\n" +
                    "**KDR:** " + kdr + "\n\n" +
                    "**Winstreak:** " + winStreak + "\n" +
                    "**Melhor:** " + highWinStreak + "\n" +
                    "**WLR:** " + wlr + "\n\n" +
                    "**Camas quebradas:** " + beds + "\n" +
                    "**Camas perdidas:** " + bedsLost + "\n" +
                    "**BBLR:** " + bblr + "\n\n" +
                    "**Final Kills:** " + finalKills + "\n\n" +
                    "**Rank:** " + rankedTier;

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.decode("#1a7a1a"))
                    .setTitle(account.getName() + " - " + modeName)
                    .setThumbnail(avatarUrl)
                    .setDescription(description);

            responder.addEmbed(embed).update().join();
            return;
        }

        ArcadeCategory arcade;
        try {
            arcade = ArcadeCategory.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            responder.setContent("❌ Modo inválido.").update().join();
            return;
        }

        // Buscar dados diretamente do PostgreSQL sem instanciar BedMember (evita dependência do Bukkit)
        JsonObject statsJson = fetchBedStats(account.getId().toString(), arcade);
        if (statsJson == null) {
            responder.setContent("❌ **" + nick + "** não possui dados de BedWars.").update().join();
            return;
        }

        int matches    = getInt(statsJson, "matches");
        int wins       = getInt(statsJson, "wins");
        int defeats    = getInt(statsJson, "defeats");
        int kills      = getInt(statsJson, "kills");
        int deaths     = getInt(statsJson, "deaths");
        int finalKills = getInt(statsJson, "finalKills");
        int beds       = getInt(statsJson, "bedDestruction");
        int bedsLost   = matches - wins;
        int winStreak      = getInt(statsJson, "winStreak");
        int highWinStreak  = getInt(statsJson, "highWinStreak");

        String winPct = matches == 0 ? "0%" : (Math.round((double) wins / matches * 1000.0) / 10.0) + "%";
        double kdr  = deaths   == 0 ? kills : Math.round((double) kills / deaths   * 100.0) / 100.0;
        double wlr  = defeats  == 0 ? wins  : Math.round((double) wins  / defeats  * 100.0) / 100.0;
        double bblr = bedsLost == 0 ? beds  : Math.round((double) beds  / bedsLost * 100.0) / 100.0;

        String modeName  = "BedWars " + arcade.getName();
        String avatarUrl = String.format(Constant.MINOTAR_AVATAR_API_URL, account.getName(), 120);

        String description =
                "**Partidas:** " + matches + "\n" +
                "**Vitórias:** " + wins + " (" + winPct + ")\n" +
                "**Derrotas:** " + defeats + "\n\n" +
                "**Kills:** " + kills + "\n" +
                "**Deaths:** " + deaths + "\n" +
                "**KDR:** " + kdr + "\n\n" +
                "**Winstreak:** " + winStreak + "\n" +
                "**Melhor:** " + highWinStreak + "\n" +
                "**WLR:** " + wlr + "\n\n" +
                "**Camas quebradas:** " + beds + "\n" +
                "**Camas perdidas:** " + bedsLost + "\n" +
                "**BBLR:** " + bblr + "\n\n" +
                "**Final Kills:** " + finalKills;

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.decode("#1a7a1a"))
                .setTitle(account.getName() + " - " + modeName)
                .setThumbnail(avatarUrl)
                .setDescription(description);

        responder.addEmbed(embed).update().join();
    }

    /**
     * Busca os stats do BedWars diretamente do PostgreSQL sem usar classes do Bukkit.
     */
    private JsonObject fetchBedStats(String accountId, ArcadeCategory arcade) {
        String sql = "SELECT data FROM members_bedwars WHERE id = ?";
        try (Connection conn = Core.getPostgres().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            String json = rs.getString("data");
            JsonObject root = Core.GSON.fromJson(json, JsonObject.class);

            // Navegar até metadata.statsList e encontrar o stats do arcade correto
            if (!root.has("metadata")) return null;
            JsonObject metadata = root.getAsJsonObject("metadata");
            if (!metadata.has("statsList")) return null;
            JsonArray statsList = metadata.getAsJsonArray("statsList");

            for (JsonElement el : statsList) {
                JsonObject stats = el.getAsJsonObject();
                if (!stats.has("arcade")) continue;
                String arcadeName = stats.get("arcade").getAsString();
                if (arcadeName.equalsIgnoreCase(arcade.name())) {
                    return stats;
                }
            }

        } catch (Exception e) {
            Core.getLogger().warning("[StatsSlashCommand] Erro ao buscar stats: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca e combina os stats de todos os modos de BedWars.
     */
    private JsonObject fetchCombinedBedStats(String accountId) {
        String sql = "SELECT data FROM members_bedwars WHERE id = ?";
        try (Connection conn = Core.getPostgres().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            String json = rs.getString("data");
            JsonObject root = Core.GSON.fromJson(json, JsonObject.class);

            // Buscar level e xp do context (não do metadata)
            int level = 0;
            int xp = 0;
            if (root.has("context")) {
                JsonObject context = root.getAsJsonObject("context");
                level = getInt(context, "level");
                xp = getInt(context, "levelXp");
            }
            
            // Buscar pontos de ranked do metadata
            int rankedPoints = 0;
            if (root.has("metadata")) {
                JsonObject metadata = root.getAsJsonObject("metadata");
                rankedPoints = getInt(metadata, "rankedPoints");
            }

            if (!root.has("metadata")) return null;
            JsonObject metadata = root.getAsJsonObject("metadata");
            if (!metadata.has("statsList")) return null;
            JsonArray statsList = metadata.getAsJsonArray("statsList");

            // Somar todos os stats de BedWars
            JsonObject combined = new JsonObject();
            int totalMatches = 0, totalWins = 0, totalDefeats = 0;
            int totalKills = 0, totalDeaths = 0, totalFinalKills = 0, totalBeds = 0;
            int maxWinStreak = 0, maxHighWinStreak = 0;

            for (JsonElement el : statsList) {
                JsonObject stats = el.getAsJsonObject();
                if (!stats.has("arcade")) continue;
                String arcadeName = stats.get("arcade").getAsString();
                
                // Somar apenas modos de BedWars
                if (arcadeName.startsWith("BEDWARS_")) {
                    totalMatches += getInt(stats, "matches");
                    totalWins += getInt(stats, "wins");
                    totalDefeats += getInt(stats, "defeats");
                    totalKills += getInt(stats, "kills");
                    totalDeaths += getInt(stats, "deaths");
                    totalFinalKills += getInt(stats, "finalKills");
                    totalBeds += getInt(stats, "bedDestruction");
                    
                    // Para winstreak, pegar o maior valor atual
                    int winStreak = getInt(stats, "winStreak");
                    if (winStreak > maxWinStreak) maxWinStreak = winStreak;
                    
                    // Para highWinStreak, pegar o maior valor histórico
                    int highWinStreak = getInt(stats, "highWinStreak");
                    if (highWinStreak > maxHighWinStreak) maxHighWinStreak = highWinStreak;
                }
            }

            // Se não encontrou nenhum stat, retornar null
            if (totalMatches == 0 && level == 0) return null;

            // Criar objeto combinado
            combined.addProperty("level", level);
            combined.addProperty("xp", xp);
            combined.addProperty("rankedPoints", rankedPoints);
            combined.addProperty("matches", totalMatches);
            combined.addProperty("wins", totalWins);
            combined.addProperty("defeats", totalDefeats);
            combined.addProperty("kills", totalKills);
            combined.addProperty("deaths", totalDeaths);
            combined.addProperty("finalKills", totalFinalKills);
            combined.addProperty("bedDestruction", totalBeds);
            combined.addProperty("winStreak", maxWinStreak);
            combined.addProperty("highWinStreak", maxHighWinStreak);

            return combined;

        } catch (Exception e) {
            Core.getLogger().warning("[StatsSlashCommand] Erro ao buscar stats combinados: " + e.getMessage());
        }
        return null;
    }

    private String getRankedTierName(int points) {
        if (points >= 4000) return "Master";
        if (points >= 3000) return "Titan";
        if (points >= 2500) return "Gladiator";
        if (points >= 2000) return "Elite";
        if (points >= 1500) return "Veteran";
        if (points >= 1000) return "Soldier";
        if (points >= 500) return "Recruit";
        return "Rookie";
    }

    private int getInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return 0;
        JsonElement el = obj.get(key);
        return el.isJsonNull() ? 0 : el.getAsInt();
    }
}
