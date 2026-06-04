package com.minecraft.proxyhost.account.context.objects.aura;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageAuraMetadata {

    private String imageUrl = "";
    private String displayName = "";
    private String preset = "default";

    public boolean isValid() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public void clear() {
        this.imageUrl = "";
        this.displayName = "";
    }
}
