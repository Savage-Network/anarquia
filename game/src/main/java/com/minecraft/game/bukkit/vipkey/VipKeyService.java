package com.minecraft.game.bukkit.vipkey;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.api.vipkey.VipKey;
import com.minecraft.game.api.vipkey.VipKeyFormat;
import com.minecraft.game.backend.data.VipKeyData;
import com.minecraft.game.bukkit.rank.RankGrantService;
import com.minecraft.game.util.AccountSync;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class VipKeyService {

    private static final RankType[] VIP_RANKS = {
            RankType.VIP, RankType.NATIVE, RankType.EMBER, RankType.EMBER_PLUS,
            RankType.SAVAGE, RankType.BETA, RankType.PRECIOUS
    };

    private VipKeyService() {}

    public static RankType[] vipRanks() {
        return VIP_RANKS;
    }

    public static Optional<RankType> parseVipRank(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        try {
            RankType type = RankType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            if (!isVipKeyRank(type)) return Optional.empty();
            return Optional.of(type);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static boolean isVipKeyRank(RankType type) {
        if (type == null) return false;
        for (RankType allowed : VIP_RANKS) {
            if (allowed == type) return true;
        }
        return false;
    }

    public static long parseDurationMs(String tempo) {
        return RankGrantService.parseDurationMs(tempo);
    }

    public static String formatDuration(long durationMs) {
        return RankGrantService.formatDuration(durationMs);
    }

    public static VipKey generate(Player creator, RankType rankType, long durationMs) {
        VipKeyData data = Core.getVipKeyData();
        if (data == null) throw new IllegalStateException("VipKeyData indisponivel");

        String code;
        int attempts = 0;
        do {
            code = VipKeyFormat.generate();
            attempts++;
            if (attempts > 20) throw new IllegalStateException("Nao foi possivel gerar codigo unico");
        } while (data.findByCode(code).isPresent());

        VipKey key = new VipKey(
                UUID.randomUUID(),
                code,
                rankType,
                durationMs,
                creator != null ? creator.getUniqueId() : null,
                creator != null ? creator.getName() : "Console",
                System.currentTimeMillis(),
                null, null, 0L
        );
        return data.insert(key);
    }

    public static String redeem(Account account, String rawCode) {
        if (account == null) return "§cConta nao carregada.";
        if (rawCode == null || rawCode.isBlank()) return "§cUso: /resgatar <codigo>";

        VipKeyData data = Core.getVipKeyData();
        if (data == null) return "§cSistema de keys indisponivel.";

        Optional<VipKey> opt = data.findByCode(rawCode);
        if (opt.isEmpty()) return "§cKey invalida ou inexistente.";
        VipKey key = opt.get();
        if (key.isRedeemed()) {
            return "§cEsta key ja foi resgatada.";
        }

        if (!data.markRedeemed(key.getCode(), account.getId(), account.getName(), System.currentTimeMillis())) {
            return "§cEsta key ja foi resgatada.";
        }

        RankGrantService.grantTemporary(account, key.getRankType(), key.getDurationMs());
        AccountSync.saveAndBroadcast(account);
        RankGrantService.refreshOnline(account);
        RankGrantService.notifyRankReceived(account, key.getRankType());

        return "§aKey resgatada! Rank §6" + key.getRankType().name()
                + " §apor §f" + formatDuration(key.getDurationMs()) + "§a.";
    }

    public static void grantDirect(Account account, RankType rankType, long durationMs) {
        RankGrantService.grantTemporary(account, rankType, durationMs);
        AccountSync.saveAndBroadcast(account);
        RankGrantService.refreshOnline(account);
        RankGrantService.notifyRankReceived(account, rankType);
    }

    public static List<VipKey> listUnredeemed(int limit) {
        VipKeyData data = Core.getVipKeyData();
        if (data == null) return List.of();
        return data.listUnredeemed(limit);
    }
}
