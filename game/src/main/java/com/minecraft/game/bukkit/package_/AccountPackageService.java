package com.minecraft.game.bukkit.package_;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.tag.Tag;
import com.minecraft.game.account.package_.AccountPackage;
import com.minecraft.game.bukkit.rank.RankGrantService;
import org.bukkit.Bukkit;

public final class AccountPackageService {

    private AccountPackageService() {}

    public static PackageOutcome apply(Account account, AccountPackage pkg) {
        if (account == null || pkg == null) {
            return PackageOutcome.fail("Conta ou pacote invalido.");
        }

        if (pkg.hasMoney()) account.addGolds(pkg.getMoney());
        if (pkg.hasCash()) account.addCash(pkg.getCash());

        if (pkg.hasRank()) {
            if (pkg.getRankDurationMs() != null && pkg.getRankDurationMs() > 0) {
                RankGrantService.grantTemporary(account, pkg.getRank(), pkg.getRankDurationMs());
            } else {
                RankGrantService.grantPermanent(account, pkg.getRank());
            }
        }

        if (pkg.hasTag()) {
            account.setTag(pkg.getTag());
        }

        if (pkg.hasMedal()) {
            account.addMedal(pkg.getMedal());
            account.setMedal(pkg.getMedal());
        }

        account.markDirty();
        return PackageOutcome.ok("Pacote §e" + pkg.getDisplayName() + " §aaplicado em §f"
                + account.getName() + "§a. §7(" + pkg.describeContents() + "§7)");
    }

    public static PackageOutcome remove(Account account, AccountPackage pkg) {
        if (account == null || pkg == null) {
            return PackageOutcome.fail("Conta ou pacote invalido.");
        }

        if (pkg.hasMoney()) {
            account.setGolds(Math.max(0, account.getGolds() - pkg.getMoney()));
        }
        if (pkg.hasCash()) {
            account.setCash(Math.max(0, account.getCash() - pkg.getCash()));
        }

        if (pkg.hasMedal()) account.removeMedal(pkg.getMedal());

        if (pkg.hasRank()) {
            RankGrantService.removeRank(account, pkg.getRank());
        }

        if (pkg.hasTag()) {
            account.setTag(Tag.of(account.getRankType()));
        }

        account.markDirty();
        return PackageOutcome.ok("Pacote §e" + pkg.getDisplayName() + " §cremovido de §f"
                + account.getName() + "§c.");
    }

    public static void notifyApplied(Account account, AccountPackage pkg) {
        if (account == null || pkg == null || !account.isOnline()) return;
        Runnable task = () -> {
            if (!account.isOnline()) return;
            account.send("§aVocê recebeu o pacote §e" + pkg.getDisplayName() + "§a!");
        };
        runSync(task);
    }

    public static void notifyRemoved(Account account, AccountPackage pkg) {
        if (account == null || pkg == null || !account.isOnline()) return;
        Runnable task = () -> {
            if (!account.isOnline()) return;
            account.send("§cO pacote §e" + pkg.getDisplayName() + " §cfoi removido da sua conta.");
        };
        runSync(task);
    }

    private static void runSync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), task);
        }
    }

    public record PackageOutcome(boolean success, String message) {
        static PackageOutcome ok(String message) {
            return new PackageOutcome(true, message);
        }

        static PackageOutcome fail(String message) {
            return new PackageOutcome(false, message);
        }
    }
}
