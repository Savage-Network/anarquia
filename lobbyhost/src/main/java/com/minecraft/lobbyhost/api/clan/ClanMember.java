package com.minecraft.lobbyhost.api.clan;

import com.minecraft.lobbyhost.account.Account;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanMember {

    private final UUID id;
    private String name;
    private ClanRole role;
    private final long joinedAt;

    public ClanMember(Account account, ClanRole role) {
        this.id = account.getId();
        this.name = account.getName();
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
    }

    public boolean hasRole(ClanRole minimum) {
        return role.ordinal() >= minimum.ordinal();
    }
}
