package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.ability;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.ability.enums.AbilityType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class LobbyAbilityMenu extends Menu {

    private final BedMember member;

    public LobbyAbilityMenu(Player player, BedMember member) {
        super(player, "Habilidades", 4);

        this.member = member;
    }

    @Override
    public void handle() {
        clear();
        addItem(4, buildCoinsDisplay());

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        AbilityType[] abilities = AbilityType.values();
        
        for (int i = 0; i < abilities.length && i < slots.length; i++) {
            addItem(slots[i], buildAbilityItem(abilities[i]));
        }

        addCloseButton();

        display();
    }

    protected Item buildCoinsDisplay() {
        Item icon = Item.of(Material.GOLD_INGOT)
                .flags(ItemFlag.values())
                .enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>();
        lore.add("§7Moedas disponíveis: §e" + member.getCoins());
        lore.add("");
        lore.add("§7Ganhe moedas:");
        lore.add("§8• §7Jogando partidas");
        lore.add("§8• §7Vitórias e kills");
        lore.add("§8• §7Completando desafios");

        icon.name("§6§lSuas Moedas")
                .lore(lore);

        return icon;
    }

    protected Item buildAbilityItem(AbilityType type) {
        Item icon = type.getIcon().clone()
                .flags(ItemFlag.values());

        String abilityName = type.name().toLowerCase();
        boolean hasPurchased = member.hasPurchasedAbility(abilityName);
        boolean isSelected = member.hasSelectedAbility(abilityName);
        boolean canPurchase = member.getCoins() >= type.getCost();

        List<String> lore = new ArrayList<>(type.getDescription());
        lore.add("");
        lore.add("§7Custo: " + type.getCostFormatted());
        lore.add("");

        if (hasPurchased) {
            if (isSelected) {
                lore.add("§a✓ Selecionada!");
                icon.enchantment(Enchantment.DURABILITY, 1);
            } else {
                lore.add("§eClique para selecionar!");
            }
        } else if (canPurchase) {
            lore.add("§eClique para comprar!");
        } else {
            int remaining = type.getCost() - member.getCoins();
            lore.add("§cFaltam §e" + remaining + " moedas§c.");
        }

        icon.name((hasPurchased ? (isSelected ? "§a" : "§e") : canPurchase ? "§e" : "§c") + type.getName());
        icon.lore(lore);

        icon.click(event -> {
            if (hasPurchased) {
                // Selecionar/Deselecionar habilidade
                if (isSelected) {
                    member.deselectAbility(abilityName);
                    sound(MenuSound.SUCCESS);
                    getPlayer().sendMessage("§eVocê desmarcou a habilidade §7" + type.getName() + "§e.");
                } else {
                    member.selectAbility(abilityName);
                    sound(type.getPurchaseSound());
                    getPlayer().sendMessage("§aVocê selecionou a habilidade §e" + type.getName() + "§a!");
                }
                handle();
            } else {
                // Comprar habilidade
                if (!canPurchase) {
                    sound(MenuSound.ERROR);
                    int remaining = type.getCost() - member.getCoins();
                    getPlayer().sendMessage("§cVocê não tem moedas suficientes! Faltam §e" + remaining + " moedas§c.");
                    return;
                }

                sound(type.getPurchaseSound());

                member.removeCoins(type.getCost());
                member.purchaseAbility(abilityName);
                member.selectAbility(abilityName); // Selecionar automaticamente após comprar

                getPlayer().sendMessage("§aVocê comprou e selecionou a habilidade §e" + type.getName() + "§a!");
                
                handle();
            }
        });

        return icon;
    }
}

