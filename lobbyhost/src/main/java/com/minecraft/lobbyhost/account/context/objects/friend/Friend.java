package com.minecraft.lobbyhost.account.context.objects.friend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    private UUID id;
    private String name;
    private long since;
    private boolean favorite;
}
