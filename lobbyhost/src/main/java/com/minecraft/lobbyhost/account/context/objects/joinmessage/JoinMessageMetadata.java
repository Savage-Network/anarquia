package com.minecraft.lobbyhost.account.context.objects.joinmessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinMessageMetadata {

    private String selectedIdentifier = "";

    public boolean isUsing() {
        return selectedIdentifier != null && !selectedIdentifier.isEmpty();
    }
}
