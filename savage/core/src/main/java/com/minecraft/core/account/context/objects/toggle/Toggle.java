package com.minecraft.core.account.context.objects.toggle;

import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Toggle {

    /* Member Toggle List */
    private boolean allowMessages = true, allowFriendRequests = true,
            allowPartyInvite = true, allowClanInvite = true, allowStatsView = true,
            allowClanTag = true, isConfirmToHub = false;

    private boolean visiblePlayersHub = true;

    /* VIP Toggle List */
    private boolean allowJoinMessage = true;
    private TagPrefix tagPrefix = TagPrefix.DEFAULT;

    /* Staff Toggle List */
    private boolean allowLogs = true, allowBuild = false;

    private boolean autoVanish = true, staffChat = false;
}
