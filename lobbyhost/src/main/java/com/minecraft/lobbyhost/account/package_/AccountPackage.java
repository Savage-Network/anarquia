package com.minecraft.lobbyhost.account.package_;

import com.minecraft.lobbyhost.account.context.objects.medal.Medal;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.bukkit.rank.RankGrantService;
import lombok.Getter;

@Getter
public final class AccountPackage {

    private final String id;
    private final String displayName;
    private final int money;
    private final int cash;
    private final RankType rank;
    private final Long rankDurationMs;
    private final Tag tag;
    private final Medal medal;

    private AccountPackage(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.money = Math.max(0, builder.money);
        this.cash = Math.max(0, builder.cash);
        this.rank = builder.rank;
        this.rankDurationMs = builder.rankDurationMs;
        this.tag = builder.tag;
        this.medal = builder.medal;
    }

    public static Builder create(String id, String displayName) {
        return new Builder(id, displayName);
    }

    public boolean hasMoney() {
        return money > 0;
    }

    public boolean hasCash() {
        return cash > 0;
    }

    public boolean hasRank() {
        return rank != null;
    }

    public boolean hasTag() {
        return tag != null;
    }

    public boolean hasMedal() {
        return medal != null && medal != Medal.NONE;
    }

    public String describeContents() {
        StringBuilder sb = new StringBuilder();
        if (hasMoney()) sb.append("§6+$").append(money).append(" money§7, ");
        if (hasCash()) sb.append("§b+$").append(cash).append(" cash§7, ");
        if (hasRank()) {
            sb.append("§6").append(rank.name());
            if (rankDurationMs != null && rankDurationMs > 0) {
                sb.append(" §7(").append(RankGrantService.formatDuration(rankDurationMs)).append(")");
            }
            sb.append("§7, ");
        }
        if (hasTag()) sb.append("tag ").append(tag.name()).append("§7, ");
        if (hasMedal()) sb.append("medalha ").append(medal.name()).append("§7, ");
        if (sb.isEmpty()) return "§7(vazio)";
        return sb.substring(0, sb.length() - 2);
    }

    public static final class Builder {
        private final String id;
        private final String displayName;
        private int money;
        private int cash;
        private RankType rank;
        private Long rankDurationMs;
        private Tag tag;
        private Medal medal;

        private Builder(String id, String displayName) {
            this.id = id.toLowerCase();
            this.displayName = displayName;
        }

        public Builder money(int amount) {
            this.money = amount;
            return this;
        }

        public Builder cash(int amount) {
            this.cash = amount;
            return this;
        }

        public Builder rank(RankType rank) {
            this.rank = rank;
            return this;
        }

        public Builder rankDuration(String tempo) {
            this.rankDurationMs = RankGrantService.parseDurationMs(tempo);
            return this;
        }

        public Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        public Builder medal(Medal medal) {
            this.medal = medal;
            return this;
        }

        public AccountPackage build() {
            return new AccountPackage(this);
        }
    }
}
