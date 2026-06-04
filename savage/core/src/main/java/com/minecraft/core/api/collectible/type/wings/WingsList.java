package com.minecraft.core.api.collectible.type.wings;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum WingsList {

    ANJO("Asas de Anjo", CollectibleRarity.EPIC,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    DEMONIO("Asas Sombrias", CollectibleRarity.MYTHICAL,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    FOGO("Asas de Fogo", CollectibleRarity.EPIC,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    GELO("Asas de Gelo", CollectibleRarity.RARE,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0=");

    private final WingsCollectible cosmetic;

    WingsList(String name, CollectibleRarity rarity, String skull) {
        WingsCollectible wing = new WingsCollectible(name, rarity, Arrays.asList(RankType.VIP, RankType.SAVAGE), System.currentTimeMillis());
        wing.addFrames(skull);
        wing.setLore(Arrays.asList("§7Asas decorativas nas costas.", "", "§7Raridade: " + rarity.getColoredName()));
        this.cosmetic = wing;
    }
}
