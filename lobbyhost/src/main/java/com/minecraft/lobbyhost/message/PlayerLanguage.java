package com.minecraft.lobbyhost.message;

import java.util.Locale;

public enum PlayerLanguage {

    PT("pt", Locale.forLanguageTag("pt-BR")),
    EN("en", Locale.forLanguageTag("en-US"));

    private final String id;
    private final Locale locale;

    PlayerLanguage(String id, Locale locale) {
        this.id = id;
        this.locale = locale;
    }

    public String id() {
        return id;
    }

    public Locale locale() {
        return locale;
    }

    public static PlayerLanguage fromId(String raw) {
        if (raw != null && raw.equalsIgnoreCase(EN.id)) {
            return EN;
        }
        return PT;
    }
}
