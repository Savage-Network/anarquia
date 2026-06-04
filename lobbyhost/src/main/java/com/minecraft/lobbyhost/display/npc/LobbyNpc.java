package com.minecraft.lobbyhost.display.npc;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NPC de lobby 1.21: {@link Interaction} (clique) + {@link ItemDisplay} (cabeca)
 * + linhas {@link TextDisplay} (holograma).
 * 
 * Suporta escala customizavel (padrao 1.5x maior que um jogador) e rotacao.
 */
@Getter
public final class LobbyNpc {

    public static final NamespacedKey NPC_ID_KEY =
            NamespacedKey.minecraft("lobbyhost_npc_id");

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();
    
    // Escala padrão: 1.5x maior que um jogador normal
    private static final float DEFAULT_SCALE = 1.5f;

    private final NpcDefinition definition;
    private final UUID interactionId;
    private final UUID itemDisplayId;  // ID do ItemDisplay para rotação
    private final List<UUID> entityIds = new ArrayList<>();
    private final float scale;

    public LobbyNpc(NpcDefinition definition, Location base) {
        this(definition, base, definition.getScale());
    }
    
    public LobbyNpc(NpcDefinition definition, Location base, float scale) {
        this.definition = definition;
        this.scale = scale;
        UUID[] result = spawnBundle(base);
        this.interactionId = result[0];
        this.itemDisplayId = result[1];
    }

    private UUID[] spawnBundle(Location base) {
        if (base.getWorld() == null) throw new IllegalStateException("Mundo invalido para NPC " + definition.getId());

        float yaw = base.getYaw();
        
        // Ajusta altura da cabeça baseado na escala (1.35 é altura padrão, multiplicado pela escala)
        double headHeight = 1.35 * scale;
        Location headLoc = base.clone().add(0, headHeight, 0);
        headLoc.setYaw(yaw);

        // Zona de clique (ajustada para a escala do NPC)
        // Largura e altura escaladas proporcionalmente
        float interactionWidth = 0.9f * scale;
        float interactionHeight = 2.0f * scale;
        
        Interaction interaction = base.getWorld().spawn(base, Interaction.class, ent -> {
            ent.setInteractionWidth(interactionWidth);
            ent.setInteractionHeight(interactionHeight);
            ent.setResponsive(true);
            ent.setPersistent(false);
            ent.getPersistentDataContainer().set(NPC_ID_KEY, PersistentDataType.STRING, definition.getId());
        });

        // ItemDisplay com escala aplicada
        ItemStack displayItem;
        
        // Se custom-item está definido, usa ele ao invés de cabeça de jogador
        if (definition.getCustomItem() != null && !definition.getCustomItem().isBlank()) {
            try {
                org.bukkit.Material material = org.bukkit.Material.valueOf(definition.getCustomItem().toUpperCase());
                displayItem = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                // Fallback para cabeça se o material não existir
                displayItem = SkullProfiles.createHead("Steve");
            }
        } else {
            // Usa cabeça de jogador (comportamento padrão)
            displayItem = (definition.getSkinTexture() != null && !definition.getSkinTexture().isBlank())
                    ? SkullProfiles.createHeadFromTexture(definition.getSkinTexture(), definition.getSkinSignature())
                    : SkullProfiles.createHead(definition.getSkinPlayer() != null ? definition.getSkinPlayer() : "Steve");
        }

        final ItemStack headStack = displayItem;
        ItemDisplay headDisplay = base.getWorld().spawn(headLoc, ItemDisplay.class, id -> {
            id.setItemStack(headStack);
            id.setBillboard(Display.Billboard.CENTER);
            id.setPersistent(false);
            
            // Aplica transformação de escala
            Transformation transform = id.getTransformation();
            Vector3f newScale = new Vector3f(scale, scale, scale);
            id.setTransformation(new Transformation(
                transform.getTranslation(),
                transform.getLeftRotation(),
                newScale,
                transform.getRightRotation()
            ));
            
            id.getPersistentDataContainer().set(NPC_ID_KEY, PersistentDataType.STRING, definition.getId());
        });
        entityIds.add(headDisplay.getUniqueId());

        // Holograma (ajustado para a escala)
        List<String> lines = definition.getHologramLines();
        double lineHeight = 0.28;
        double startY = 2.05 * scale; // Altura inicial escalada
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isEmpty()) continue;
            Location holo = base.clone().add(0, startY + (lines.size() - 1 - i) * lineHeight, 0);
            TextDisplay td = base.getWorld().spawn(holo, TextDisplay.class, text -> {
                text.text(legacy(line));
                text.setBillboard(Display.Billboard.CENTER);
                text.setSeeThrough(true);
                text.setShadowed(true);
                text.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                text.setPersistent(false);
                text.getPersistentDataContainer().set(NPC_ID_KEY, PersistentDataType.STRING, definition.getId());
            });
            entityIds.add(td.getUniqueId());
        }

        entityIds.add(interaction.getUniqueId());
        return new UUID[]{interaction.getUniqueId(), headDisplay.getUniqueId()};
    }
    
    /**
     * Retorna o ItemDisplay deste NPC (para rotação)
     */
    public ItemDisplay getItemDisplay() {
        Entity entity = org.bukkit.Bukkit.getEntity(itemDisplayId);
        return entity instanceof ItemDisplay ? (ItemDisplay) entity : null;
    }

    public void remove() {
        for (UUID id : new ArrayList<>(entityIds)) {
            Entity e = org.bukkit.Bukkit.getEntity(id);
            if (e != null && !e.isDead()) e.remove();
        }
        entityIds.clear();
    }

    public static Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
