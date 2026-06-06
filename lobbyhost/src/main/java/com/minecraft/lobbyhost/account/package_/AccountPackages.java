package com.minecraft.lobbyhost.account.package_;

import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AccountPackages {

    private static final Map<String, AccountPackage> BY_ID = new LinkedHashMap<>();

    public static final AccountPackage VIP_BASICO = register(
            AccountPackage.create("vip_basico", "VIP Básico")
                    .money(10_000)
                    .cash(50)
                    .rank(RankType.VIP)
                    .tag(Tag.VIP)
                    .medal(Medal.ESTRELINHA)
                    .build());

    public static final AccountPackage VIP_30D = register(
            AccountPackage.create("vip_30d", "VIP 30 Dias")
                    .money(5_000)
                    .cash(25)
                    .rank(RankType.VIP)
                    .rankDuration("30d")
                    .tag(Tag.VIP)
                    .medal(Medal.ESTRELINHA)
                    .build());

    public static final AccountPackage SAVAGE_COMPLETO = register(
            AccountPackage.create("savage", "Savage Completo")
                    .money(50_000)
                    .cash(200)
                    .rank(RankType.SAVAGE)
                    .tag(Tag.SAVAGE)
                    .medal(Medal.SOLAR)
                    .build());

    public static final AccountPackage NATIVE_PACOTE = register(
            AccountPackage.create("native", "Native")
                    .money(25_000)
                    .cash(100)
                    .rank(RankType.NATIVE)
                    .tag(Tag.NATIVE)
                    .medal(Medal.GALAXIA)
                    .build());

    public static final AccountPackage BETA = register(
            AccountPackage.create("beta", "Beta Tester")
                    .money(35_000)
                    .cash(150)
                    .rank(RankType.BETA)
                    .tag(Tag.TAG_BETA)
                    .medal(Medal.COMETA)
                    .build());

    public static final AccountPackage ECONOMIA_START = register(
            AccountPackage.create("economia", "Pacote Economia")
                    .money(15_000)
                    .cash(75)
                    .build());

    private AccountPackages() {}

    private static AccountPackage register(AccountPackage pkg) {
        BY_ID.put(pkg.getId().toLowerCase(Locale.ROOT), pkg);
        return pkg;
    }

    public static Optional<AccountPackage> find(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        return Optional.ofNullable(BY_ID.get(raw.trim().toLowerCase(Locale.ROOT)));
    }

    public static Collection<AccountPackage> all() {
        return BY_ID.values();
    }

    public static String idsJoined() {
        return BY_ID.keySet().stream().collect(Collectors.joining("§7, §e"));
    }
}
