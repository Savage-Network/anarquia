package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.type.particle.ParticleType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ParticleTypeSelectorMenu extends Menu {

    private final Account account;
    private final Collectible collectible;

    public ParticleTypeSelectorMenu(Player player, Collectible collectible, Menu last) {
        super(player, "Tipo de Partícula", last, 4);

        this.account = Core.getAccountController().of(player.getUniqueId());
        this.collectible = collectible;
    }

    @Override
    public void handle() {
        clear();

        ParticleType currentType = getCurrentType();

        int slot = 10;
        for (ParticleType type : ParticleType.values()) {
            boolean isSelected = currentType != null && type == currentType;

            addItem(slot++, Item.of(isSelected ? Material.EMERALD_BLOCK : type.getIcon())
                    .name((isSelected ? "§a" : "§7") + type.getName())
                    .lore(type.getDescription(),
                            "",
                            isSelected ? "§a✓ Selecionado" : "§eClique para selecionar!")
                    .click(event -> {
                        setParticleType(type);
                        close();
                        sound(MenuSound.SUCCESS);
                        
                        // Obter operador existente ou criar novo
                        com.minecraft.core.api.collectible.operator.CollectibleOperator operator = 
                                com.minecraft.core.api.collectible.operator.CollectibleOperator.of(getPlayer(), 
                                        com.minecraft.core.api.collectible.CollectibleCategory.PARTICLE);
                        
                        if (operator != null) {
                            // Se já existe um operador, apenas atualizar o tipo e re-handle
                            operator.handle(collectible);
                        } else {
                            // Se não existe, criar novo operador
                            com.minecraft.core.api.collectible.operator.CollectibleOperator.start(getPlayer(), collectible);
                        }
                        
                        account.send("§ePartícula §6" + collectible.getName() + " §eativada com tipo §6" + type.getName() + "§e!");
                    }));

            // Adicionar linha nova a cada 7 itens
            if (slot % 9 == 8) {
                slot += 2;
            }
        }

        if (isReturnable()) addBackButton();

        display();
    }

    private ParticleType getCurrentType() {
        // Verificar se há um operador ativo primeiro
        com.minecraft.core.api.collectible.operator.CollectibleOperator operator = 
                com.minecraft.core.api.collectible.operator.CollectibleOperator.of(getPlayer(), 
                        com.minecraft.core.api.collectible.CollectibleCategory.PARTICLE);
        
        if (operator == null || operator.getCollectible() == null) {
            return null; // Não há partícula ativa
        }
        
        // Verificar se a partícula ativa é a mesma que estamos visualizando
        if (!operator.getCollectible().getIdentifier().equals(collectible.getIdentifier())) {
            return null;
        }
        
        return com.minecraft.core.api.collectible.operator.list.ParticleOperator.getParticleType(
                account.getId(), collectible.getIdentifier());
    }

    private void setParticleType(ParticleType type) {
        com.minecraft.core.api.collectible.operator.list.ParticleOperator.setParticleType(
                account.getId(), collectible.getIdentifier(), type);
    }
}

