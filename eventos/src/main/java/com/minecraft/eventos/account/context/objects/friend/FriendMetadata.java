package com.minecraft.eventos.account.context.objects.friend;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class FriendMetadata {

    private Map<UUID, Friend> friends = new HashMap<>();
    private List<FriendRequest> requests = new ArrayList<>();
}
