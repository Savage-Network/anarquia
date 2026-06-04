package com.minecraft.proxyhost.proxy;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MOTD, contagem de jogadores e modo manutencao (por rank minimo).
 * Persistido em {@code proxy-settings.yml} e editavel via {@code /proxy}.
 */
public final class ProxySettingsManager {

    @Getter private static ProxySettingsManager instance;

    private final Path file;

    private String motdLine1 = "&6&lSAVAGE &7| &fSemi-Anarquia";
    private String motdLine2 = "&7Jogue agora";
    private int maxPlayersOverride = -1;
    private boolean hidePlayerSample;

    private boolean maintenanceEnabled;
    @Getter private RankType maintenanceMinRank = RankType.HELPER;
    private String maintenanceMotdLine1 = "&c&lMANUTENCAO";
    private String maintenanceMotdLine2 = "&7Apenas staff autorizado pode entrar.";
    private List<String> maintenanceKickMessage = defaultKickLines();

    public ProxySettingsManager(Path dataDirectory) {
        this.file = dataDirectory.resolve("proxy-settings.yml");
        instance = this;
    }

    public static ProxySettingsManager get() {
        return instance;
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
                if (root == null) return;
                applyRoot(root);
            }
            Core.getLogger().info("proxy-settings.yml carregado (manutencao={}, rank>={}).",
                    maintenanceEnabled, maintenanceMinRank.name());
        } catch (Exception e) {
            Core.getLogger().error("Falha ao carregar proxy-settings.yml: " + e.getMessage(), e);
        }
    }

    public void save() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();

            Map<String, Object> motd = new LinkedHashMap<>();
            motd.put("line1", motdLine1);
            motd.put("line2", motdLine2);
            motd.put("max-players", maxPlayersOverride);
            motd.put("hide-sample", hidePlayerSample);
            root.put("motd", motd);

            Map<String, Object> maint = new LinkedHashMap<>();
            maint.put("enabled", maintenanceEnabled);
            maint.put("min-rank", maintenanceMinRank.name());
            maint.put("motd-line1", maintenanceMotdLine1);
            maint.put("motd-line2", maintenanceMotdLine2);
            maint.put("kick-message", new ArrayList<>(maintenanceKickMessage));
            root.put("maintenance", maint);

            Yaml yaml = new Yaml();
            Files.writeString(file, yaml.dump(root));
        } catch (Exception e) {
            Core.getLogger().error("Falha ao salvar proxy-settings.yml", e);
        }
    }

    public void reload() {
        load();
    }

    /* ----- MOTD ----- */

    public String getMotdLine1() { return motdLine1; }
    public String getMotdLine2() { return motdLine2; }

    public void setMotdLine1(String line) {
        this.motdLine1 = line == null ? "" : line;
        save();
    }

    public void setMotdLine2(String line) {
        this.motdLine2 = line == null ? "" : line;
        save();
    }

    public void resetMotd() {
        motdLine1 = "&6&lSAVAGE &7| &fSemi-Anarquia";
        motdLine2 = "&7Jogue agora — &6loja.savage.com.br";
        save();
    }

    public int getMaxPlayersOverride() { return maxPlayersOverride; }

    public void setMaxPlayersOverride(int value) {
        this.maxPlayersOverride = value;
        save();
    }

    public boolean isHidePlayerSample() { return hidePlayerSample; }

    public void setHidePlayerSample(boolean hide) {
        this.hidePlayerSample = hide;
        save();
    }

    public Component buildMotdDescription(ProxyServer proxy) {
        String l1 = maintenanceEnabled ? maintenanceMotdLine1 : motdLine1;
        String l2 = maintenanceEnabled ? maintenanceMotdLine2 : motdLine2;
        String p1 = applyPlaceholders(l1, proxy);
        String p2 = applyPlaceholders(l2, proxy);
        return LegacyTexts.component(p1).append(Component.newline()).append(LegacyTexts.component(p2));
    }

    public int resolveMaxPlayers(com.velocitypowered.api.proxy.server.ServerPing ping) {
        if (maxPlayersOverride >= 0) return maxPlayersOverride;
        return ping.asBuilder().getMaximumPlayers();
    }

    public int displayOnlinePlayers(ProxyServer proxy) {
        return proxy.getPlayerCount();
    }

    /* ----- Manutencao ----- */

    public boolean isMaintenanceEnabled() { return maintenanceEnabled; }

    public void setMaintenanceEnabled(boolean enabled) {
        this.maintenanceEnabled = enabled;
        save();
    }

    public void setMaintenanceMinRank(RankType rank) {
        this.maintenanceMinRank = rank == null ? RankType.HELPER : rank;
        save();
    }

    public void setMaintenanceKickMessage(List<String> lines) {
        this.maintenanceKickMessage = lines == null || lines.isEmpty()
                ? defaultKickLines() : new ArrayList<>(lines);
        save();
    }

    public void setMaintenanceMotdLine1(String line) {
        this.maintenanceMotdLine1 = line == null ? "" : line;
        save();
    }

    public void setMaintenanceMotdLine2(String line) {
        this.maintenanceMotdLine2 = line == null ? "" : line;
        save();
    }

    public Component maintenanceKickComponent() {
        return LegacyTexts.joinLines(LegacyTexts.colorize(maintenanceKickMessage));
    }

    public boolean canJoin(Account account, CommandSource source) {
        if (!maintenanceEnabled) return true;
        if (!(source instanceof Player player)) return true;
        if (player.hasPermission("proxyhost.maintenance.bypass")) return true;
        if (account == null) return false;
        return account.getRankType().isAtLeast(maintenanceMinRank);
    }

    public void kickNonStaff(ProxyServer proxy) {
        if (!maintenanceEnabled) return;
        Component msg = maintenanceKickComponent();
        for (Player p : proxy.getAllPlayers()) {
            if (p.hasPermission("proxyhost.maintenance.bypass")) continue;
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a != null && a.getRankType().isAtLeast(maintenanceMinRank)) continue;
            p.disconnect(msg);
        }
    }

    private String applyPlaceholders(String line, ProxyServer proxy) {
        if (line == null) return "";
        String out = line.replace('&', '§');
        out = out.replace("{online}", String.valueOf(proxy.getPlayerCount()));
        int maxDisplay = maxPlayersOverride >= 0 ? maxPlayersOverride
                : Core.getPlugin().getConfig().getInt("motd.default-max-display", 100);
        out = out.replace("{max}", String.valueOf(maxDisplay));
        out = out.replace("{name}", Core.getPlugin().getConfig().getString("server.name", "Savage"));
        out = out.replace("{maintenance}", maintenanceEnabled ? "§cSIM" : "§aNAO");
        return out;
    }

    @SuppressWarnings("unchecked")
    private void applyRoot(Map<String, Object> root) {
        Object motdObj = root.get("motd");
        if (motdObj instanceof Map<?, ?> motd) {
            motdLine1 = str(motd.get("line1"), motdLine1);
            motdLine2 = str(motd.get("line2"), motdLine2);
            maxPlayersOverride = num(motd.get("max-players"), maxPlayersOverride);
            hidePlayerSample = bool(motd.get("hide-sample"), hidePlayerSample);
        }

        Object maintObj = root.get("maintenance");
        if (maintObj instanceof Map<?, ?> maint) {
            maintenanceEnabled = bool(maint.get("enabled"), maintenanceEnabled);
            maintenanceMinRank = parseRank(str(maint.get("min-rank"), maintenanceMinRank.name()));
            maintenanceMotdLine1 = str(maint.get("motd-line1"), maintenanceMotdLine1);
            maintenanceMotdLine2 = str(maint.get("motd-line2"), maintenanceMotdLine2);
            Object kick = maint.get("kick-message");
            if (kick instanceof List<?> list) {
                List<String> lines = new ArrayList<>();
                for (Object o : list) lines.add(o == null ? "" : o.toString());
                if (!lines.isEmpty()) maintenanceKickMessage = lines;
            }
        }
    }

    private void copyDefault() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/proxy-settings.yml")) {
            if (in == null) throw new IllegalStateException("proxy-settings.yml default ausente.");
            Files.copy(in, file);
        }
    }

    private static RankType parseRank(String name) {
        try {
            return RankType.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return RankType.HELPER;
        }
    }

    private static String str(Object v, String def) {
        return v == null ? def : v.toString();
    }

    private static int num(Object v, int def) {
        if (v instanceof Number n) return n.intValue();
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }

    private static boolean bool(Object v, boolean def) {
        if (v instanceof Boolean b) return b;
        if (v == null) return def;
        return Boolean.parseBoolean(v.toString());
    }

    private static List<String> defaultKickLines() {
        return new ArrayList<>(List.of(
                "&6&lSAVAGE",
                "",
                "&cO servidor esta em manutencao.",
                "&7Apenas membros da equipe podem entrar.",
                "&7Volte em breve!"
        ));
    }
}
