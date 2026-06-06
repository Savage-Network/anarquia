package com.minecraft.lobbyhost.bukkit.fake;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.fake.Fake;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.skin.SkinMetadata;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class FakeService {

    private static final Random RANDOM = ThreadLocalRandom.current();

    private FakeService() {}

    public static boolean canUse(Account account) {
        if (!FakeConfig.isEnabled()) return false;
        if (account == null) return false;
        if (FakeConfig.isRequireVip()) {
            return account.isVIP() || account.hasPermission("game.fake");
        }
        return account.hasPermission("game.fake");
    }

    public static String normalize(String raw) {
        if (raw == null) return null;
        String nick = raw.trim();
        if (nick.length() < 3 || nick.length() > 16) return null;
        if (!nick.matches("^[a-zA-Z0-9_]+$")) return null;
        return nick;
    }

    public static String validate(Account account, String nick) {
        String normalized = normalize(nick);
        if (normalized == null) {
            return "§cNick inválido. Use 3-16 caracteres (a-z, 0-9, _).";
        }
        if (normalized.equalsIgnoreCase(account.getName())) {
            return "§cUse um nick diferente do seu nome real.";
        }
        if (FakeConfig.isBlocked(normalized)) {
            return "§cEste nick não pode ser usado.";
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().equalsIgnoreCase(normalized)) {
                return "§cEste jogador está online com esse nick.";
            }
            Account other = Core.getAccountController().of(online.getUniqueId());
            if (other != null && other.isFake()
                    && normalized.equalsIgnoreCase(other.getNickname())
                    && !other.getId().equals(account.getId())) {
                return "§cAlguém já está usando este fake nick.";
            }
        }
        return null;
    }

    public static String randomNick() {
        return switch (FakeConfig.getRandomMode()) {
            case LIST -> pickFromList();
            case GENERATE -> generateNick();
            case MIXED -> RANDOM.nextBoolean() ? pickFromList() : generateNick();
        };
    }

    private static String pickFromList() {
        var names = FakeConfig.getRandomNames();
        if (names.isEmpty()) {
            return generateNick();
        }
        return names.get(RANDOM.nextInt(names.size()));
    }

    private static String generateNick() {
        for (int attempt = 0; attempt < 40; attempt++) {
            String prefix = pick(FakeConfig.getRandomPrefixes());
            String suffix = pick(FakeConfig.getRandomSuffixes());
            int variant = RANDOM.nextInt(1000);
            String raw = prefix + suffix + variant;
            if (raw.length() > 16) {
                raw = raw.substring(0, 16);
            }
            if (raw.length() >= 3 && raw.matches("^[a-zA-Z0-9_]+$")) {
                return raw;
            }
        }
        return "Player" + RANDOM.nextInt(9000 + 1000);
    }

    public static void enable(Account account, Player player, String nick,
                              Consumer<String> onSuccess, Consumer<String> onFailure) {
        if (!canUse(account)) {
            onFailure.accept("§cApenas VIP+ pode usar fake nick.");
            return;
        }
        String error = validate(account, nick);
        if (error != null) {
            onFailure.accept(error);
            return;
        }
        if (!account.isStaffer() && account.hasCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY)) {
            long left = account.getCooldownRemaining(Constant.NICK_CHANGE_COOLDOWN_KEY);
            onFailure.accept("§cAguarde §e" + Math.max(1, left / 1000L) + "s §cpara trocar de nick.");
            return;
        }

        Fake fake = account.getContext().getFake();
        if (fake.isValid()) {
            fake.setLastNick(fake.getNick());
        }
        fake.setNick(nick);
        fake.setUpdatedAt(System.currentTimeMillis());
        account.markDirty();

        applyIdentity(account, player, () -> {
            if (!account.isStaffer()) {
                account.setCooldown(Constant.NICK_CHANGE_COOLDOWN_KEY, FakeConfig.getCooldownMs());
            }
            TagManager.updateTag(account);
            onSuccess.accept("§aFake nick ativado: §f" + nick);
        }, () -> onFailure.accept("§cNão foi possível aplicar a skin deste nick."));
    }

    public static void disable(Account account, Player player, Consumer<String> onSuccess) {
        Fake fake = account.getContext().getFake();
        if (!fake.isValid()) {
            onSuccess.accept("§eVocê não está usando fake nick.");
            return;
        }
        fake.setLastNick(fake.getNick());
        fake.setNick("");
        fake.setUpdatedAt(System.currentTimeMillis());
        account.markDirty();

        restoreIdentity(account, player, () -> {
            TagManager.updateTag(account);
            onSuccess.accept("§aFake nick removido. Identidade restaurada.");
        });
    }

    public static void applyIdentity(Account account, Player player, Runnable onReady, Runnable onSkinFail) {
        if (account == null || player == null || !player.isOnline()) return;

        String nick = account.getNickname();
        if (FakeConfig.isChangeProfileName()) {
            try {
                PlayerProfile profile = player.getPlayerProfile();
                profile.setName(nick);
                player.setPlayerProfile(profile);
            } catch (Throwable ignored) {}
        }

        Component display = Component.text(nick);
        player.displayName(display);
        player.playerListName(display);

        if (!FakeConfig.isApplySkin()) {
            if (onReady != null) onReady.run();
            return;
        }

        SkinService.fetchMojang(nick).thenAccept(opt -> Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
            if (!player.isOnline() || account == null || !account.isFake()) return;
            if (opt.isEmpty()) {
                if (onSkinFail != null) onSkinFail.run();
                if (onReady != null) onReady.run();
                return;
            }
            SkinService.apply(player, opt.get());
            if (onReady != null) onReady.run();
        }));
    }

    public static void restoreIdentity(Account account, Player player, Runnable onReady) {
        if (account == null || player == null || !player.isOnline()) return;

        String real = account.getName();
        if (FakeConfig.isChangeProfileName()) {
            try {
                PlayerProfile profile = player.getPlayerProfile();
                profile.setName(real);
                player.setPlayerProfile(profile);
            } catch (Throwable ignored) {}
        }

        TagManager.updateTag(account);

        SkinService.apply(account, player);
        if (onReady != null) onReady.run();
    }

    public static void refreshIfFake(Account account, Player player) {
        if (account == null || player == null || !player.isOnline() || !account.isFake()) return;
        applyIdentity(account, player, null, null);
        TagManager.updateTag(account);
    }

    public static boolean shouldHideIdentity(Account target, Account viewer) {
        if (target == null || !target.isFake()) return false;
        if (viewer != null && viewer.hasPermission("game.fake.see")) return false;
        return FakeConfig.isHideRank();
    }

    public static Tag displayTag(Account account, Account viewer) {
        if (shouldHideIdentity(account, viewer)) return Tag.MEMBER;
        return account.getTag();
    }

    public static RankType displayRank(Account account, Account viewer) {
        if (shouldHideIdentity(account, viewer)) return RankType.MEMBER;
        return account.getRankType();
    }

    private static String pick(java.util.List<String> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
