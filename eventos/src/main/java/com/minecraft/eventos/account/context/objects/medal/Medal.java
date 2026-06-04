package com.minecraft.eventos.account.context.objects.medal;

import lombok.Getter;

/**
 * Catalogo completo de medalhas. Espelha
 * {@code com.minecraft.core.account.medal.Medal} do Savage.
 *
 * <p>Aparecem apenas no chat (antes do prefixo da tag) e em previews
 * de menu. Posicao no enum determina o "pool VIP proporcional"
 * (ver {@code Account.hasMedal} - §4.2 do prompt).
 */
@Getter
public enum Medal {

    /* especiais zero */
    NONE        ("Nenhuma",        "",          "§7"),
    TOWER       ("Torre",          "T",         "§7"),

    /* TOP / icones decorativos */
    TOP_DARK_AQUA("Top Dark Aqua", "❁",         "§3"),
    TOP_DARK_RED ("Top Dark Red",  "❥",         "§4"),
    TOP_DARK_BLUE("Top Dark Blue", "❤",         "§1"),
    TOP_AQUA     ("Top Aqua",      "✦",         "§b"),

    /* Pool VIP */
    MESTRE      ("Mestre",         "✠",         "§6"),
    HALLOWEEN   ("Halloween",      "✟",         "§6"),
    ESTRELINHA  ("Estrelinha",     "⚝",         "§e"),
    COMANDO     ("Comando",        "✜",         "§4"),
    CRUZ_SAGRADA("Cruz Sagrada",   "☩",         "§f"),
    GALAXIA     ("Galaxia",        "✦",         "§5"),
    CONSTELACAO ("Constelacao",    "✯",         "§d"),
    NOVA        ("Nova",           "✵",         "§b"),
    BRILHO      ("Brilho",         "✧",         "§e"),
    SOLAR       ("Solar",          "☀",         "§6"),
    LUNAR       ("Lunar",          "☾",         "§7"),
    ROSA_DOURADA("Rosa Dourada",   "❀",         "§6"),
    FLOR_REAL   ("Flor Real",      "✿",         "§d"),
    PRIMOR      ("Primor",         "❁",         "§5"),
    PRIMAVERA   ("Primavera",      "❃",         "§a"),
    NOTA_BAIXA  ("Nota Baixa",     "♩",         "§2"),
    NOTA_MEDIA  ("Nota Media",     "♪",         "§a"),
    MUSICO      ("Musico",         "♫",         "§e"),
    MAESTRO     ("Maestro",        "♬",         "§6"),
    NEVASCA     ("Nevasca",        "❆",         "§b"),
    RAFA        ("Rafa",           "❥",         "§c"),
    APROVADO    ("Aprovado",       "✓",         "§a"),

    /* Vermelho exclusivo */
    Y           ("Y",              "➤",         "§c"),

    /* Pool VIP cont */
    ZEN         ("Zen",            "☯",         "§f"),
    TOXIC       ("Toxic",          "☣",         "§a"),
    RAIO        ("Raio",           "✶",         "§e"),
    COMETA      ("Cometa",         "✷",         "§b"),
    ORBITA      ("Orbita",         "✸",         "§9"),
    CORONA      ("Corona",         "✹",         "§6"),
    SUPERNOVA   ("Supernova",      "✺",         "§d"),
    FORTUNA     ("Fortuna",        "℧",         "§6"),
    PULSO       ("Pulso",          "✻",         "§b"),
    IMPACTO     ("Impacto",        "✼",         "§4"),
    VORTEX      ("Vortex",         "✽",         "§5"),
    AURORA      ("Aurora",         "✾",         "§d"),
    ASSINATURA  ("Assinatura",     "✐",         "§e"),
    CEM         ("100",            "✫",         "§6"),

    /* Exclusivas (tag/rank) */
    CHAMPION    ("Champion",       "❂",         "§e"),
    PRECIOUS    ("Precious",       "♦",         "§5"),

    /* Estilo livre */
    CAVEIRA     ("Ceifador",       "☠",         "§8"),
    NEGADO      ("Negado",         "✘",         "§c"),
    C           ("C",              "§lC",       "§4"),
    GAY         ("Gay",            "§c§l⚐",     "§d"),
    FCF         ("Feijao com Farinha", "§5§l6§d§l7", "§5"),
    APRENDIZ    ("Aprendiz",       "§5✎",       "§5"),
    PROTAGONISTA("Prota",          "§6➤",       "§6"),
    PP          ("PinkP",          "§d§l❀",     "§d"),
    AJUDANTE    ("Ajudante",       "⚒",         "§3"),
    ESPADAS     ("Espadas",        "§6§l⚔",     "§6"),
    STAR        ("Estrela",        "✯",         "§e"),
    USD         ("USD",            "$",         "§a"),
    DRAKE       ("Drake",          "§b§lD",     "§b");

    private final String displayName;
    private final String symbol;
    private final String color;

    Medal(String displayName, String symbol, String color) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
    }

    public String getPermission() {
        return "medal." + name().toLowerCase();
    }

    public String getColoredSymbol() {
        if (symbol == null || symbol.isEmpty()) return "";
        // Simbolos que ja embarcam a propria cor (§5§l...) ficam intactos.
        return symbol.contains("§") ? symbol + "§r " : color + symbol + " ";
    }

    public String getColoredName() {
        return color + displayName;
    }

    /** Resolve por enum name OU display name (case-insensitive). */
    public static Medal of(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        for (Medal m : values()) {
            if (m.name().equalsIgnoreCase(trimmed)) return m;
            if (m.displayName.equalsIgnoreCase(trimmed)) return m;
            if (m.displayName.replace(" ", "").equalsIgnoreCase(trimmed)) return m;
        }
        return null;
    }
}
