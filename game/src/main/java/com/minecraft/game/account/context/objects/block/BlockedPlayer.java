package com.minecraft.game.account.context.objects.block;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Equivalente ao {@code Block} do Savage. Renomeado para evitar conflito
 * com {@link org.bukkit.block.Block} em imports.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedPlayer {

    private UUID id;
    private long blockedAt = System.currentTimeMillis();

    public BlockedPlayer(UUID id) {
        this.id = id;
    }
}
