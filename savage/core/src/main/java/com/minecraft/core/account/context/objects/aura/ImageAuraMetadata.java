package com.minecraft.core.account.context.objects.aura;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageAuraMetadata {

    /** URL direta da imagem (Imgur, i.imgur.com, etc.). */
    private String imageUrl;

    /** Nome exibido ao selecionar (ex.: Yin-Yang). */
    private String displayName;

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    public void clear() {
        imageUrl = null;
        displayName = null;
    }
}
