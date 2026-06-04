package com.minecraft.auth.account.context.objects.cosmetic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CosmeticEntry {

    private String identifier;
    private long acquiredAt = System.currentTimeMillis();

    public CosmeticEntry(String identifier) {
        this.identifier = identifier;
    }
}
