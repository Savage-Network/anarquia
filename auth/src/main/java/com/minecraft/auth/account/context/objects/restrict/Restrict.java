package com.minecraft.auth.account.context.objects.restrict;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restrict {

    private String key;
    private long expiresAt;
    private String reason;

    public boolean hasExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }
}
