package com.minecraft.proxyhost.account.context.objects.friend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {

    private UUID sender;
    private long sentAt;
}
