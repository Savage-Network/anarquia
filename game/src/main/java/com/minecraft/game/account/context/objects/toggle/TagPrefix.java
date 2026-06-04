package com.minecraft.game.account.context.objects.toggle;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Espelho de {@code com.minecraft.core.account.tag.TagPrefix} do Savage.
 *
 * <p>Controla COMO o jogador (viewer) ve o prefixo das tags dos demais.
 * Persistido em {@code Toggle.tagPrefix}. Alterado por {@code /prefixtype}.
 */
@Getter
public enum TagPrefix {

    DEFAULT             ("Padrao"),
    COLOR               ("Cor"),
    BRACKETS            ("Colchetes"),
    BRACKETS_NORMAL_CASE("Colchetes normais"),
    DEFAULT_WHITE       ("Padrao branco"),
    DEFAULT_GRAY        ("Padrao cinza");

    private final String displayName;

    TagPrefix(String displayName) {
        this.displayName = displayName;
    }

    public String getKey() {
        return name().toLowerCase();
    }

    public static List<TagPrefix> list() {
        return Arrays.asList(values());
    }

    public static TagPrefix of(String raw) {
        if (raw == null) return null;
        String norm = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        for (TagPrefix p : values()) {
            if (p.name().equals(norm)) return p;
            if (p.displayName.equalsIgnoreCase(raw)) return p;
        }
        return null;
    }
}
