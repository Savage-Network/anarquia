package com.minecraft.game.account.context.objects.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * No projeto single-server o "route" indica apenas o mundo/spawn de destino.
 * O placeholder mantem a forma do Savage para compatibilidade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteContext {

    private UUID senderId;
    private String targetWorld;
    private long updatedAt;
}
