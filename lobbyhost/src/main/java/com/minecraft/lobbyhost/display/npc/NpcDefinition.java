package com.minecraft.lobbyhost.display.npc;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Definicao estatica de um NPC de lobby (carregada de {@code npcs.yml}).
 * 
 * Suporta escala customizavel (padrao 1.5x) e diferentes tipos de display (cabeca, picareta, etc).
 */
@Getter
@Builder
public final class NpcDefinition {

    private final String id;
    /** Chave em {@code architect.json} ({@code npc_bw}, etc.). */
    private final String locationKey;
    private final String displayName;
    @Builder.Default
    private final List<String> hologramLines = Collections.emptyList();
    /** Nome de jogador para cabeca (skin Mojang). */
    private final String skinPlayer;
    /** Textura base64 (opcional, sobrescreve skinPlayer). */
    private final String skinTexture;
    private final String skinSignature;
    /** Tipo de item customizado (ex: DIAMOND_PICKAXE, DIAMOND_SWORD, etc). Se definido, sobrescreve skin. */
    private final String customItem;
    @Builder.Default
    private final NpcActionType actionType = NpcActionType.MESSAGE;
    private final String actionValue;
    /** Escala do NPC (1.0 = tamanho normal, 1.5 = 50% maior). */
    @Builder.Default
    private final float scale = 1.5f;
    /** Se true, gira o item customizado (desligado por padrao — NPC fica parado). */
    @Builder.Default
    private final boolean rotate = false;

    public static NpcDefinition fromConfig(String id, org.bukkit.configuration.ConfigurationSection sec) {
        if (sec == null) return null;
        List<String> lines = sec.getStringList("hologram");
        if (lines.isEmpty() && sec.isString("hologram")) {
            lines = List.of(sec.getString("hologram"));
        }
        String actionTypeRaw = sec.getString("action.type", "MESSAGE");
        NpcActionType type;
        try {
            type = NpcActionType.valueOf(actionTypeRaw.toUpperCase());
        } catch (Exception e) {
            type = NpcActionType.MESSAGE;
        }
        org.bukkit.configuration.ConfigurationSection skin = sec.getConfigurationSection("skin");
        String player = skin != null ? skin.getString("player", "Steve") : "Steve";
        String texture = skin != null ? skin.getString("texture", "") : "";
        String signature = skin != null ? skin.getString("signature", "") : "";
        
        // Item customizado (ex: DIAMOND_PICKAXE)
        String customItem = sec.getString("custom-item", null);
        
        // Suporte para escala customizável (padrão 1.5x)
        float scale = (float) sec.getDouble("scale", 1.5);

        return NpcDefinition.builder()
                .id(id)
                .locationKey(sec.getString("location", id))
                .displayName(sec.getString("display-name", id))
                .hologramLines(lines)
                .skinPlayer(player)
                .skinTexture(texture)
                .skinSignature(signature)
                .customItem(customItem)
                .actionType(type)
                .actionValue(sec.getString("action.value", ""))
                .scale(scale)
                .rotate(sec.getBoolean("rotate", false))
                .build();
    }
}
