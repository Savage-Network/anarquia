package com.minecraft.lobbyhost.api.punishment;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.util.PunishmentIds;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Punishment {

    private String id;
    private UUID player;
    private UUID author = Constant.DEFAULT_ID;
    private String ip = "127.0.0.1";
    private PunishmentCategory category;
    private PunishmentReason reason;
    private String cause = "Não informado";
    private String server = "proxy";
    private Revocation revocation = new Revocation();
    private long createdAt = System.currentTimeMillis();
    private long expiresAt = -1L;
    private String ruleId;
    private boolean valid = true;

    public static Punishment create(UUID player, UUID author, String ip, String server,
                                   PunishmentCategory category, PunishmentReason reason,
                                   String cause, long expiresAt) {
        return create(player, author, ip, server, category, reason, cause, expiresAt, null);
    }

    public static Punishment create(UUID player, UUID author, String ip, String server,
                                   PunishmentCategory category, PunishmentReason reason,
                                   String cause, long expiresAt, String ruleId) {
        Punishment p = new Punishment();
        p.id = PunishmentIds.newId();
        p.player = player;
        p.author = author != null ? author : Constant.DEFAULT_ID;
        p.ip = ip != null ? ip : "127.0.0.1";
        p.server = server != null ? server : "proxy";
        p.category = category;
        p.reason = reason;
        p.cause = cause != null && !cause.isBlank() ? cause : "Não informado";
        p.expiresAt = expiresAt;
        p.ruleId = ruleId;
        p.createdAt = System.currentTimeMillis();
        p.valid = true;
        p.revocation = new Revocation();
        return p;
    }

    public boolean isTemporary() {
        return expiresAt > -1L;
    }

    public boolean hasExpired() {
        return isTemporary() && expiresAt <= System.currentTimeMillis();
    }

    public boolean isActive() {
        return valid && (revocation == null || !revocation.isValid()) && !hasExpired();
    }

    public void revoke(UUID authorId, String reasonText) {
        Revocation rev = new Revocation();
        rev.setAuthor(authorId != null ? authorId : Constant.DEFAULT_ID);
        rev.setReason(reasonText != null && !reasonText.isBlank() ? reasonText : "Não informado");
        rev.setData(System.currentTimeMillis());
        this.revocation = rev;
        this.valid = false;
    }
}
