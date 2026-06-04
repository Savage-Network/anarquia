package com.minecraft.game.bukkit.kit;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class KitManager {

    private static final Map<String, KitDefinition> KITS = new LinkedHashMap<>();

    private KitManager() {}

    public static void load(JavaPlugin plugin) {
        KITS.clear();
        File file = new File(plugin.getDataFolder(), "kits.yml");
        if (!file.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("kits");
        if (root == null) {
            Core.getLogger().warning("kits.yml sem secao 'kits'.");
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            KitDefinition kit = KitDefinition.fromConfig(key, sec);
            KITS.put(kit.getId(), kit);
        }
        Core.getLogger().info("Kits carregados: " + KITS.size());
    }

    public static Map<String, KitDefinition> all() {
        return Collections.unmodifiableMap(KITS);
    }

    public static KitDefinition get(String id) {
        if (id == null) return null;
        return KITS.get(id.toLowerCase(Locale.ROOT));
    }

    public static List<KitDefinition> availableFor(Account account) {
        if (account == null) return List.of();
        List<KitDefinition> out = new ArrayList<>();
        for (KitDefinition kit : KITS.values()) {
            if (canUse(account, kit)) out.add(kit);
        }
        return out;
    }

    public static boolean canUse(Account account, KitDefinition kit) {
        if (kit == null) return false;
        if (!account.getRankType().isAtLeast(kit.getMinRank())) return false;
        String perm = kit.getPermission();
        if (perm != null && !perm.isBlank() && !account.hasPermission(perm)) return false;
        return true;
    }

    public static String apply(Account account, Player player, String kitId) {
        KitDefinition kit = get(kitId);
        if (kit == null) return "§cKit desconhecido.";
        if (!canUse(account, kit)) return "§cVoce nao tem acesso a este kit.";

        if (!account.isStaffer() && kit.getCooldownMs() > 0 && account.hasCooldown(kit.cooldownKey())) {
            long left = account.getCooldownRemaining(kit.cooldownKey());
            return "§cAguarde " + Math.max(1, left / 1000L) + "s para usar este kit novamente.";
        }

        kit.apply(player);
        if (!account.isStaffer() && kit.getCooldownMs() > 0) {
            account.setCooldown(kit.cooldownKey(), kit.getCooldownMs());
        }
        account.save();
        return "§aKit " + kit.getDisplayName() + " §aaplicado.";
    }

    public static void sendList(Account account) {
        account.send("§6§lKITS §7disponiveis:");
        for (KitDefinition kit : KITS.values()) {
            String status = canUse(account, kit) ? "§a✔" : "§c✖";
            long left = account.getCooldownRemaining(kit.cooldownKey());
            String cd = (!account.isStaffer() && left > 0)
                    ? " §8(" + (left / 1000L) + "s)"
                    : "";
            account.send(status + " §f/kit " + kit.getId() + " §7- " + kit.getDisplayName() + cd);
        }
    }
}
