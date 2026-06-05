package com.minecraft.proxyhost.whitelist;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Whitelist por alvo (proxy/lobby/game/eventos) com rank mínimo,
 * ranks extras e bypass de jogadores.
 */
public final class WhitelistManager {

    @Getter private static WhitelistManager instance;

    private final Path file;
    private final EnumMap<WhitelistTarget, WhitelistRule> rules = new EnumMap<>(WhitelistTarget.class);

    public WhitelistManager(Path dataDirectory) {
        this.file = dataDirectory.resolve("whitelist.yml");
        instance = this;
        for (WhitelistTarget target : WhitelistTarget.values()) {
            rules.put(target, new WhitelistRule());
        }
    }

    public static WhitelistManager get() {
        return instance;
    }

    public WhitelistRule rule(WhitelistTarget target) {
        return rules.computeIfAbsent(target, k -> new WhitelistRule());
    }

    public boolean isEnabled(WhitelistTarget target) {
        return rule(target).isEnabled();
    }

    public void load() {
        try {
            if (!Files.exists(file)) {
                copyDefault();
            }
            try (InputStream in = Files.newInputStream(file)) {
                Yaml yaml = new Yaml();
                @SuppressWarnings("unchecked")
                Map<String, Object> root = yaml.load(in);
                applyRoot(root == null ? Map.of() : root);
            }
            Core.getLogger().info("whitelist.yml carregado.");
        } catch (Exception e) {
            Core.getLogger().error("Falha ao carregar whitelist.yml: " + e.getMessage(), e);
        }
    }

    public void reload() {
        load();
    }

    public void save() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            for (WhitelistTarget target : WhitelistTarget.values()) {
                WhitelistRule rule = rule(target);
                Map<String, Object> section = new LinkedHashMap<>();
                section.put("enabled", rule.isEnabled());
                section.put("min-rank", rule.getMinRank().name());
                List<String> ranks = new ArrayList<>();
                for (RankType rank : rule.getExtraRanks()) ranks.add(rank.name());
                section.put("extra-ranks", ranks);

                List<String> players = new ArrayList<>();
                for (Map.Entry<UUID, String> entry : rule.getBypassPlayers().entrySet()) {
                    players.add(entry.getKey() + "|" + entry.getValue());
                }
                section.put("players", players);
                root.put(target.name().toLowerCase(Locale.ROOT), section);
            }
            Yaml yaml = new Yaml();
            Files.writeString(file, yaml.dump(root));
        } catch (Exception e) {
            Core.getLogger().error("Falha ao salvar whitelist.yml", e);
        }
    }

    public boolean canAccess(Account account, Player player, WhitelistTarget target) {
        if (target == null) return true;
        WhitelistRule rule = rule(target);
        if (!rule.isEnabled()) return true;
        if (player != null && player.hasPermission("proxyhost.whitelist.bypass")) return true;
        if (account == null) return false;

        if (rule.getBypassPlayers().containsKey(account.getId())) return true;

        RankType rank = account.getRankType();
        if (rule.getExtraRanks().contains(rank)) return true;
        return rank.isAtLeast(rule.getMinRank());
    }

    public boolean canAccessProxy(Account account, Player player) {
        return canAccess(account, player, WhitelistTarget.PROXY);
    }

    @SuppressWarnings("unchecked")
    private void applyRoot(Map<String, Object> root) {
        for (WhitelistTarget target : WhitelistTarget.values()) {
            WhitelistRule rule = rule(target);
            rule.setEnabled(false);
            rule.setMinRank(RankType.HELPER);
            rule.getExtraRanks().clear();
            rule.getBypassPlayers().clear();

            Object sectionObj = root.get(target.name().toLowerCase(Locale.ROOT));
            if (!(sectionObj instanceof Map<?, ?> section)) continue;

            rule.setEnabled(bool(section.get("enabled"), false));
            rule.setMinRank(parseRank(str(section.get("min-rank"), RankType.HELPER.name())));

            Object ranksObj = section.get("extra-ranks");
            if (ranksObj instanceof List<?> list) {
                for (Object o : list) {
                    RankType rank = parseRank(str(o, null));
                    if (rank != null) rule.getExtraRanks().add(rank);
                }
            }

            Object playersObj = section.get("players");
            if (playersObj instanceof List<?> list) {
                for (Object o : list) {
                    parsePlayerEntry(str(o, ""), rule);
                }
            } else if (playersObj instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    parsePlayerEntry(str(entry.getKey(), "") + "|" + str(entry.getValue(), ""), rule);
                }
            }
        }
    }

    private void parsePlayerEntry(String raw, WhitelistRule rule) {
        if (raw == null || raw.isBlank()) return;
        String[] parts = raw.split("\\|", 2);
        try {
            UUID id = UUID.fromString(parts[0].trim());
            String name = parts.length > 1 ? parts[1].trim() : parts[0].trim();
            rule.getBypassPlayers().put(id, name);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void copyDefault() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/whitelist.yml")) {
            if (in == null) throw new IllegalStateException("whitelist.yml default ausente.");
            Files.copy(in, file);
        }
    }

    private static RankType parseRank(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return RankType.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return null;
        }
    }

    private static String str(Object v, String def) {
        return v == null ? def : v.toString();
    }

    private static boolean bool(Object v, boolean def) {
        if (v instanceof Boolean b) return b;
        if (v == null) return def;
        return Boolean.parseBoolean(v.toString());
    }
}
