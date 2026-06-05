package com.minecraft.eventos.account.context.objects.skin;

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

    public static SkinMetadata fromTextures(String value, String signature, String displayName) {
        SkinMetadata skin = new SkinMetadata();
        skin.value = value != null ? value : "";
        skin.signature = signature != null ? signature : "";
        skin.displayName = displayName != null ? displayName : "";
        skin.updatedAt = System.currentTimeMillis();
        return skin;
    }
}
