package com.minecraft.game.api.auction;

import lombok.Getter;
import org.bukkit.Material;

import java.util.UUID;

@Getter
public final class AuctionListing {

    private final UUID id;
    private final UUID sellerId;
    private final String sellerName;
    private final Material material;
    private final int amount;
    private final int price;
    private final long createdAt;
    private final long expiresAt;

    public AuctionListing(UUID id, UUID sellerId, String sellerName, Material material,
                          int amount, int price, long createdAt, long expiresAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
