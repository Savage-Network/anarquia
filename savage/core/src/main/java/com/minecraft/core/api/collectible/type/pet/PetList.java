package com.minecraft.core.api.collectible.type.pet;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;

@Getter
public enum PetList {

    CACHORRO("Cachorro", CollectibleRarity.COMUM,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    GATO("Gato", CollectibleRarity.COMUM,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    PANDA("Panda", CollectibleRarity.RARE,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    DRAGAO("Dragãozinho", CollectibleRarity.EPIC,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    COELHO("Coelho", CollectibleRarity.RARE,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    CORUJA("Coruja", CollectibleRarity.EPIC,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0="),
    SAVAGE_PET("Pet Savage", CollectibleRarity.MYTHICAL,
            "eyJ0aW1lc3RhbXAiOjE3MDAwMDAwMDAwMDAsInByb2ZpbGVJZCI6IjdjYTY5YzQzZDJjNDg4ZDg5OTU2OTQ3ZDY0YzRiMzQ0IiwicHJvZmlsZU5hbWUiOiJNaW5lY29uIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I2NGY2NDgyNDFlMzQzZmM5Y2E0ODA1NzBiOGQ2Zjg5MmRhNjA0YjNlZjA1ZjcyZjQ5ODMzNmM0In19fX0=");

    private final PetCollectible cosmetic;

    PetList(String displayName, CollectibleRarity rarity, String skull) {
        PetCollectible pet = new PetCollectible(displayName, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP), System.currentTimeMillis());
        pet.addFrames(skull);
        pet.setLore(Arrays.asList("§7Pet exclusivo SavageMC", "", "§7Raridade: " + rarity.getColoredName()));
        this.cosmetic = pet;
    }
}
