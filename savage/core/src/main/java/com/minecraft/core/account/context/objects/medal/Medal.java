package com.minecraft.core.account.context.objects.medal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Medal {

    NONE("Nenhuma", "", ChatColor.WHITE),
    TOWER("Torre", "T", ChatColor.AQUA),

    TOP_DARK_AQUA("TOP_DARK_AQUA", "❁", ChatColor.DARK_AQUA),
    TOP_DARK_RED("TOP_DARK_RED", "❥", ChatColor.DARK_RED),
    TOP_DARK_BLUE("TOP_DARK_BLUE", "❤", ChatColor.DARK_BLUE),
    TOP_AQUA("TOP_AQUA", "✦", ChatColor.AQUA),

    MESTRE("Mestre", "✠", ChatColor.GOLD),
    HALLOWEEN("Halloween", "✟", ChatColor.DARK_PURPLE),
    ESTRELINHA("Estrelinha", "⚝", ChatColor.YELLOW),
    COMANDO("Comando", "✜", ChatColor.DARK_RED),
    CRUZ_SAGRADA("Cruz Sagrada", "☩", ChatColor.AQUA),
    GALAXIA("Galaxia", "✦", ChatColor.DARK_AQUA),
    CONSTELACAO("Constelacao", "✯", ChatColor.BLUE),
    NOVA("Nova", "✵", ChatColor.RED),
    BRILHO("Brilho", "✧", ChatColor.LIGHT_PURPLE),
    SOLAR("Solar", "☀", ChatColor.GOLD),
    LUNAR("Lunar", "☾", ChatColor.DARK_PURPLE),
    ROSA_DOURADA("Rosa Dourada", "❀", ChatColor.YELLOW),
    FLOR_REAL("Flor Real", "✿", ChatColor.GREEN),
    PRIMOR("Primor", "❁", ChatColor.AQUA),
    PRIMAVERA("Primavera", "❃", ChatColor.DARK_GREEN),
    NOTA_BAIXA("Nota Baixa", "♩", ChatColor.WHITE),
    NOTA_MEDIA("Nota Media", "♪", ChatColor.GRAY),
    MUSICO("Musico", "♫", ChatColor.DARK_AQUA),
    MAESTRO("Maestro", "♬", ChatColor.DARK_BLUE),
    NEVASCA("Nevasca", "❆", ChatColor.AQUA),
    RAFA("Rafa", "❥", ChatColor.DARK_RED),
    APROVADO("Aprovado", "✓", ChatColor.GREEN),
    Y("Y", "➤", ChatColor.RED),
    ZEN("Zen", "☯", ChatColor.LIGHT_PURPLE),
    TOXIC("Toxic", "☣", ChatColor.DARK_GREEN),
    RAIO("Raio", "✶", ChatColor.GOLD),
    COMETA("Cometa", "✷", ChatColor.YELLOW),
    ORBITA("Orbita", "✸", ChatColor.RED),
    CORONA("Corona", "✹", ChatColor.LIGHT_PURPLE),
    SUPERNOVA("Supernova", "✺", ChatColor.AQUA),
    FORTUNA("Fortuna", "℧", ChatColor.WHITE),
    PULSO("Pulso", "✻", ChatColor.DARK_AQUA),
    IMPACTO("Impacto", "✼", ChatColor.BLUE),
    VORTEX("Vortex", "✽", ChatColor.DARK_BLUE),
    AURORA("Aurora", "✾", ChatColor.DARK_PURPLE),
    ASSINATURA("Assinatura", "✐", ChatColor.GRAY),
    CEM("100", "✫", ChatColor.GOLD),
    CHAMPION("Champion", "❂", ChatColor.GOLD),
    /** Exclusiva do rank Precious §5♦ (&5♦). */
    PRECIOUS("Precious", "♦", ChatColor.DARK_PURPLE),
    CAVEIRA("Ceifador", "☠", ChatColor.DARK_GRAY),
    NEGADO("Negado", "✘", ChatColor.RED),
    C("C", "§lC", ChatColor.DARK_RED),
    GAY("Gay", "§c§l§kl§6§lG§e§lA§a§lY§b§l§kl§r", ChatColor.GOLD),
    FCF("Feijão com Farinha", "§5§l6§d§l7", ChatColor.DARK_RED),
    APRENDIZ("Aprendiz", "§5✎", ChatColor.DARK_PURPLE),
    PROTAGONISTA("Prota", "§6➤",  ChatColor.GOLD),
    PP("PinkP", "§d§l❀", ChatColor.LIGHT_PURPLE),
    AJUDANTE("Ajudante", "⚒", ChatColor.GOLD),
    ESPADAS("Espada", "§6§l⚔", ChatColor.GOLD),
    STAR("Estrela", "✯", ChatColor.YELLOW),
    USD("USD", "$", ChatColor.GREEN),
    DRAKE("Vivi na mão dos DRAKE", "§b§lD", ChatColor.AQUA);

    private final String name, symbol;
    private final ChatColor color;

    public static Medal of(String name) {
        return Stream.of(values())
                .filter(medal -> medal.name().equalsIgnoreCase(name) || medal.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getColoredSymbol() {
        return color + symbol;
    }

    public String getPermission() {
        return "medal." + this.name().toLowerCase();
    }
}
