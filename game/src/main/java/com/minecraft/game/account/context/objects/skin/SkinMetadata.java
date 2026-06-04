package com.minecraft.game.account.context.objects.skin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkinMetadata {

    private String value = "";
    private String signature = "";
    private String displayName = "";
    private long updatedAt;

    public boolean isValid() {
        return value != null && !value.isEmpty();
    }
}
