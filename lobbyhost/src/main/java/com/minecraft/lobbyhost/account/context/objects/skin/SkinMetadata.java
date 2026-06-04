package com.minecraft.lobbyhost.account.context.objects.skin;

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
        SkinMetadata meta = new SkinMetadata();
        meta.setValue(value);
        meta.setSignature(signature != null ? signature : "");
        meta.setDisplayName(displayName != null ? displayName : "");
        meta.setUpdatedAt(System.currentTimeMillis());
        return meta;
    }
}
