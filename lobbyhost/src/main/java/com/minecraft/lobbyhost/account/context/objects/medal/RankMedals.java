package com.minecraft.lobbyhost.account.context.objects.medal;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Catálogo de medalhas por rank — espelha {@code game.account.context.objects.medal.RankMedals}. */
public final class RankMedals {

    private RankMedals() {}

    public static Optional<Medal> defaultMedal(RankType rank) {
        if (rank == null) return Optional.empty();
        return switch (rank) {
            case VIP        -> Optional.of(Medal.ESTRELINHA);
            case NATIVE     -> Optional.of(Medal.GALAXIA);
            case EMBER, EMBER_PLUS -> Optional.of(Medal.BRILHO);
            case SAVAGE     -> Optional.of(Medal.SOLAR);
            case BETA       -> Optional.of(Medal.COMETA);
            case PRECIOUS   -> Optional.of(Medal.PRECIOUS);
            case BUILDER, HELPER -> Optional.of(Medal.AJUDANTE);
            case MOD, MODPLUS -> Optional.of(Medal.COMANDO);
            case ADMIN, DIRETOR, OWNER -> Optional.of(Medal.MAESTRO);
            case VERMELHO   -> Optional.of(Medal.Y);
            default         -> Optional.empty();
        };
    }

    public static List<Medal> explicitMedals(RankType rank) {
        if (rank == null) return List.of();
        return switch (rank) {
            case VIP -> List.of(Medal.ESTRELINHA, Medal.APROVADO, Medal.BRILHO);
            case NATIVE -> List.of(Medal.GALAXIA, Medal.CONSTELACAO, Medal.NOVA, Medal.ORBITA);
            case EMBER, EMBER_PLUS -> List.of(Medal.BRILHO, Medal.SOLAR, Medal.LUNAR, Medal.FORTUNA);
            case SAVAGE -> List.of(Medal.SOLAR, Medal.SUPERNOVA, Medal.AURORA, Medal.FORTUNA, Medal.CORONA);
            case BETA -> List.of(Medal.COMETA, Medal.ORBITA, Medal.PULSO, Medal.RAIO);
            case PRECIOUS -> List.of(Medal.PRECIOUS);
            case BUILDER -> List.of(Medal.AJUDANTE, Medal.ESPADAS);
            case HELPER -> List.of(Medal.AJUDANTE);
            case MOD, MODPLUS -> List.of(Medal.COMANDO, Medal.CRUZ_SAGRADA, Medal.IMPACTO);
            case ADMIN, DIRETOR, OWNER -> List.of(Medal.MAESTRO, Medal.MESTRE, Medal.STAR);
            case VERMELHO -> List.of(Medal.Y);
            default -> List.of();
        };
    }

    public static double vipPoolRatio(RankType rank) {
        if (rank == null) return 0.0;
        return switch (rank) {
            case SAVAGE -> 0.70;
            case EMBER_PLUS, EMBER -> 0.40;
            case NATIVE -> 0.20;
            case VIP -> 0.10;
            case BETA -> 0.50;
            default -> 0.0;
        };
    }

    public static int vipPoolAllowed(RankType rank, int poolSize) {
        double ratio = vipPoolRatio(rank);
        if (ratio <= 0 || poolSize <= 0) return 0;
        return (int) Math.round(poolSize * ratio);
    }

    public static boolean isGrantedByRank(RankType rank, Medal medal) {
        if (rank == null || medal == null) return false;
        if (defaultMedal(rank).filter(m -> m == medal).isPresent()) return true;
        return explicitMedals(rank).contains(medal);
    }

    public static List<String> permissionNodes(RankType rank) {
        List<String> nodes = new ArrayList<>();
        for (Medal medal : allRankMedals(rank)) {
            nodes.add(medal.getPermission());
        }
        return nodes;
    }

    public static List<Medal> allRankMedals(RankType rank) {
        List<Medal> out = new ArrayList<>();
        defaultMedal(rank).ifPresent(out::add);
        for (Medal medal : explicitMedals(rank)) {
            if (!out.contains(medal)) out.add(medal);
        }
        return Collections.unmodifiableList(out);
    }

    public static void applyRankMedals(Account account, RankType rank) {
        if (account == null || rank == null) return;
        for (Medal medal : allRankMedals(rank)) {
            account.addMedal(medal);
        }
        defaultMedal(rank).ifPresent(defaultMedal -> {
            if (account.getMedal() == Medal.NONE) {
                account.setMedal(defaultMedal);
            }
        });
    }
}
