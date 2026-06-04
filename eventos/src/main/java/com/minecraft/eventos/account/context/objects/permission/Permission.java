package com.minecraft.eventos.account.context.objects.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    private String key;
    private long givenAt;
    private long expiresAt;

    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }
}
