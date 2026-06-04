package com.minecraft.proxyhost.account.context.objects.joinmessage;

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
