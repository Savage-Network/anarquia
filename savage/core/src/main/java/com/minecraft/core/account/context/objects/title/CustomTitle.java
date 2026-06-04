package com.minecraft.core.account.context.objects.title;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CustomTitle {

    private UUID owner;
    private String title;
    private long createdAt;

    public CustomTitle(UUID owner, String title) {
        this.owner = owner;
        this.title = title;
        this.createdAt = System.currentTimeMillis();
    }
}
