package com.minecraft.core.account.context.objects.rank;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.rank.type.role.RankRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class Rank {

    @Builder.Default
    private final RankType type = RankType.MEMBER;

    @Builder.Default
    private final Assignment assignment = Assignment.AUTO;
    @Builder.Default
    private final UUID author = Constant.DEFAULT_ID;

    private final long assignedAt = System.currentTimeMillis();

    @Builder.Default
    private final long expiresAt = -1L;

    public String getName() {
        return getType().getName();
    }

    public String getColoredName() {
        return getType().getColoredName();
    }

    public String getAuthorName() {
        Account account = Core.getAccountData().of(author, false);

        return account != null ? account.getName() : author.equals(Constant.DEFAULT_ID) ? "CONSOLE" : "...";
    }

    public String getColor() {
        return getType().getColor().toString() + (getType().ordinal() > RankType.CREATOR.ordinal() ? "§o" : "");
    }

    public boolean isPermanent() {
        return expiresAt <= -1L;
    }

    public boolean isPayment() {
        return getType().getRole().equals(RankRole.VIP);
    }

    public boolean hasExpired() {
        return !isPermanent() && expiresAt <= System.currentTimeMillis();
    }
}
