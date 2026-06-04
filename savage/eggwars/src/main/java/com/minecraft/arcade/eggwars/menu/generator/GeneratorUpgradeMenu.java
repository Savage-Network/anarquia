package com.minecraft.arcade.eggwars.menu.generator;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.generator.config.GeneratorConfig;
import com.minecraft.arcade.eggwars.structure.generator.objects.EggOre;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.eggwars.structure.generator.objects.type.ore.OreGenerator;
import com.minecraft.arcade.eggwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorUpgradeMenu extends Menu {

    private static final GeneratorConfig config = GeneratorConfig.getInstance();
    
    private final User user;
    private final Arena arena;
    private final OreGenerator generator;

    public GeneratorUpgradeMenu(Player player, OreGenerator generator) {
        super(player, "Evoluir Gerador", 3);

        this.user = (User) User.of(player.getUniqueId());
        this.arena = user.getArena();
        this.generator = generator;
    }

    @Override
    public void handle() {
        clear();

        EggOre ore = generator.getOre();
        GeneratorLevel currentLevel = generator.getLevel();
        GeneratorLevel nextLevel = getNextLevel(currentLevel);

        addItem(13, buildUpgradeItem(ore, currentLevel, nextLevel));

        addCloseButton();

        display();
    }

    private GeneratorLevel getNextLevel(GeneratorLevel current) {
        GeneratorLevel[] levels = GeneratorLevel.values();
        int nextIndex = current.ordinal() + 1;
        
        // Limitar níveis máximos por tipo de gerador usando a configuração
        EggOre ore = generator.getOre();
        int maxLevel = config.getMaxLevel(ore);
        
        if (nextIndex < levels.length && nextIndex <= maxLevel) {
            return levels[nextIndex];
        }
        return null;
    }

    private Item buildUpgradeItem(EggOre ore, GeneratorLevel current, GeneratorLevel next) {
        Material iconMaterial = ore.getMaterial();
        if (ore == EggOre.IRON) iconMaterial = Material.IRON_INGOT;
        else if (ore == EggOre.GOLD) iconMaterial = Material.GOLD_INGOT;
        else if (ore == EggOre.DIAMOND) iconMaterial = Material.DIAMOND;

        Item icon = Item.of(iconMaterial).flags(ItemFlag.values());

        if (current != GeneratorLevel.NONE && !icon.hasEnchantments()) {
            icon.unsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Evolua o gerador de " + ore.getColor() + ore.getOreName() + "§7",
                "§7para gerar recursos mais rápido.",
                ""
        ));

        // Determinar nível máximo baseado no tipo de gerador usando a configuração
        int maxLevel = config.getMaxLevel(ore);

        for (GeneratorLevel level : GeneratorLevel.values()) {
            if (level.equals(GeneratorLevel.NONE) || level.ordinal() > maxLevel) continue;

            boolean has = generator.getLevel().ordinal() >= level.ordinal();
            int cost = generator.getUpgradeCost(level);
            // Se for nível 1 e o gerador está quebrado, usar diamantes para reparar
            Material costMaterial = (level == GeneratorLevel.ONE && current == GeneratorLevel.NONE) ? Material.DIAMOND : generator.getUpgradeMaterial();
            int updateTime = generator.getUpdateTimeForLevel(level);
            
            String costText = cost > 0 ? ", " + getMaterialColor(costMaterial) + cost + "x " + getMaterialName(costMaterial) : "";
            String timeText = "§7Gera a cada §e" + updateTime + "s";

            lore.add((has ? "§a✓" : "§c✗") + " §7Nível " + level.getTag() + ": " + timeText + costText);
        }

        // Se estiver quebrado, usar DIAMANTES para reparar, senão usar o material do gerador
        Material upgradeMaterial = (current == GeneratorLevel.NONE) ? Material.DIAMOND : generator.getUpgradeMaterial();
        int upgradeCost = next != null ? generator.getUpgradeCost(next) : 0;
        boolean canUpgrade = next != null && BukkitUtil.getItemAmount(getPlayer(), upgradeMaterial) >= upgradeCost;

        String upgradeMaterialName = next != null ? getMaterialName(upgradeMaterial) : "";
        int playerAmount = next != null ? BukkitUtil.getItemAmount(getPlayer(), upgradeMaterial) : 0;
        int needed = next != null ? generator.getUpgradeCost(next) : 0;
        
        lore.addAll(Arrays.asList(
                "",
                next == null
                        ? "§cNível máximo."
                        : canUpgrade ? "§eClique para evoluir!"
                        : "§cVocê não tem " + upgradeMaterialName + " suficientes. Faltam " + (needed - playerAmount) + "x " + upgradeMaterialName + "."
        ));

        icon.name((canUpgrade && next != null ? "§a" : "§c") + "Evoluir Gerador")
                .lore(lore)
                .click(event -> {
                    if (next == null) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cO gerador está no nível máximo.");
                        return;
                    }
                    
                    if (!canUpgrade) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem " + getMaterialName(upgradeMaterial) + " suficientes. Faltam " + (upgradeCost - playerAmount) + "x " + getMaterialName(upgradeMaterial) + ".");
                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    BukkitUtil.removeItemByAmount(getPlayer(), upgradeMaterial, upgradeCost);
                    generator.setLevel(next);

                    // Atualizar tempo de geração baseado no nível
                    generator.updateTimeForLevel();

                    // Se estava quebrado e foi reparado, iniciar spawn imediatamente
                    if (current == GeneratorLevel.NONE && next == GeneratorLevel.ONE) {
                        generator.spawn();
                    }

                    generator.updateHologramText();

                    String message = current == GeneratorLevel.NONE 
                        ? "§aGerador de " + ore.getColor() + ore.getOreName() + " §areparado!"
                        : "§aGerador de " + ore.getColor() + ore.getOreName() + " §aevoluído para o nível §c" + next.getTag() + "§a!";
                    getPlayer().sendMessage(message);

                    handle();
                });

        return icon;
    }

    private String getMaterialName(Material material) {
        return switch (material) {
            case IRON_INGOT -> "Ferros";
            case GOLD_INGOT -> "Ouros";
            case DIAMOND -> "Diamantes";
            default -> material.name();
        };
    }
    
    private String getMaterialColor(Material material) {
        return switch (material) {
            case IRON_INGOT -> "§f";
            case GOLD_INGOT -> "§e";
            case DIAMOND -> "§b";
            default -> "§7";
        };
    }
}
