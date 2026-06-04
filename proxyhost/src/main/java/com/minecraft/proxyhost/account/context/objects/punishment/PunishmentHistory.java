package com.minecraft.proxyhost.account.context.objects.punishment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PunishmentHistory {

    /** Quando {@code true}, jogador e imune (staff/dev). */
    private boolean untouchable;

    private List<String> punishmentIds = new ArrayList<>();

    public PunishmentHistory(boolean untouchable) {
        this.untouchable = untouchable;
    }
}
