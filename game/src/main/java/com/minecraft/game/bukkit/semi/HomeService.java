package com.minecraft.game.bukkit.semi;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.home.HomeMetadata;
import com.minecraft.game.account.context.objects.home.StoredLocation;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HomeService {

    private HomeService() {}

    public static int maxHomes(Account account) {
        if (account.isStaffer()) return 5;
        if (account.hasRank(com.minecraft.game.account.context.objects.rank.RankType.SAVAGE)) return 3;
        if (account.isVIP()) return 2;
        return 1;
    }

    public static String setHome(Account account, Player player, String name) {
        if (SpawnProtection.isProtected(player.getLocation())) {
            return "§cNao e possivel definir home na area protegida do spawn.";
        }
        if (CombatTagManager.isTagged(player)) {
            return CombatTagManager.blockedMessage(player);
        }

        String key = normalize(name);
        HomeMetadata homes = account.getHomes();
        if (!homes.getHomes().containsKey(key) && homes.getHomes().size() >= maxHomes(account)) {
            return "§cLimite de homes atingido (" + maxHomes(account) + ").";
        }

        homes.getHomes().put(key, StoredLocation.from(player.getLocation()));
        account.markDirty();
        return "§aHome §f" + key + " §adefinida.";
    }

    public static String deleteHome(Account account, String name) {
        String key = normalize(name);
        if (account.getHomes().getHomes().remove(key) == null) {
            return "§cHome nao encontrada.";
        }
        account.markDirty();
        return "§aHome §f" + key + " §aremovida.";
    }

    public static StoredLocation getHome(Account account, String name) {
        return account.getHomes().getHomes().get(normalize(name));
    }

    public static List<String> listHomes(Account account) {
        return new ArrayList<>(account.getContext().getHomes().getHomes().keySet());
    }

    public static String normalize(String name) {
        if (name == null || name.isBlank()) return "home";
        return name.toLowerCase(Locale.ROOT);
    }

    public static void sendList(Account account) {
        Map<String, StoredLocation> map = account.getHomes().getHomes();
        account.send("§6§lHOMES §7(" + map.size() + "/" + maxHomes(account) + ")");
        if (map.isEmpty()) {
            account.send("§7Nenhuma home. Use §f/sethome [nome]");
            return;
        }
        for (String key : map.keySet()) {
            account.send("§8- §f" + key);
        }
    }
}
