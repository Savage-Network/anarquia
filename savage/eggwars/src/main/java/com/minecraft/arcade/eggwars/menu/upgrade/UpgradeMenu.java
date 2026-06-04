package com.minecraft.arcade.eggwars.menu.upgrade;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.menu.upgrade.trap.TrapMenu;
import com.minecraft.arcade.eggwars.structure.generator.objects.level.GeneratorLevel;
import com.minecraft.arcade.eggwars.structure.team.Team;
import com.minecraft.arcade.eggwars.structure.team.objects.forge.Forge;
import com.minecraft.arcade.eggwars.structure.team.objects.upgrade.TeamUpgrade;
import com.minecraft.arcade.eggwars.structure.team.objects.upgrade.structure.armor.ArmorProtection;
import com.minecraft.arcade.eggwars.structure.team.objects.upgrade.structure.haste.Haste;
import com.minecraft.arcade.eggwars.structure.team.objects.upgrade.structure.sword.Sword;
import com.minecraft.arcade.eggwars.structure.team.objects.upgrade.structure.trap.Trap;
import com.minecraft.arcade.eggwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.Core;
import com.minecraft.core.member.list.eggwars.objects.enums.EggOre;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class UpgradeMenu extends Menu {

    private final User user;

    private final Arena arena;

    private final boolean isTrioOrQuartet;

    public UpgradeMenu(Player player) {
        super(player, "Armadilhas & Melhorias", 4);

        this.user = (User) User.of(player.getUniqueId());

        this.arena = user.getArena();

        this.isTrioOrQuartet = arena.getSlot().ordinal() >= Slot.TRIO.ordinal();
    }

    @Override
    public void handle() {
        clear();

        Team team = user.getTeam();

        addItem(10, buildForge(team));
        addItem(11, buildSword(team));
        addItem(12, buildHaste(team));
        addItem(13, buildArmor(team));
        addItem(14, buildAutoRegeneration(team));
        addItem(15, buildTrap(team));

        addCloseButton();

        display();
    }

    protected Item buildForge(Team team) {
        Item icon = Item.of(Material.FURNACE)
                .flags(ItemFlag.values());

        Forge forge = team.getForge();

        if (!forge.isLevel(GeneratorLevel.NONE) && !icon.hasEnchantments())
            icon.unsafeEnchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Expanda os seus recursos",
                "§7aprimorando a sua forja.",
                ""
        ));

        for (GeneratorLevel level : GeneratorLevel.values()) {
            if (level.equals(GeneratorLevel.NONE)) continue;

            boolean has = forge.isLevel(level);

            int cost = isTrioOrQuartet ? level.getCost() * 2 : level.getCost();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + level.ordinal() + ": §7" + level.getDescription()
                    + ", §b" + cost + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                forge.isHighest()
                        ? "§cNível máximo."
                        : forge.canPurchase(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((forge.canPurchase(getPlayer()) ? "§a" : "§c") + "Forja")
                .lore(lore)
                .click(event -> {
                    if (forge.isHighest()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua forja está no nível máximo.");

                        return;
                    }

                    if (!forge.canPurchase(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. Faltam " + user.getRemainingOre(EggOre.DIAMOND, forge.getNextCost()) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(EggOre.DIAMOND, forge.getNextCost());

                    forge.upgrade();

                    team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu a forja para o nível §c" + forge.getLevel().getTag() + "§e!"));

                    handle();
                });

        return icon;
    }

    protected Item buildSword(Team team) {
        Item icon = Item.of(Material.DIAMOND_SWORD)
                .flags(ItemFlag.values());

        Sword sword = team.getSword();

        if (sword != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas espadas",
                "§7mais poderosas.",
                ""
        ));

        for (Sword swordLevel : Sword.values()) {
            if (isTrioOrQuartet && swordLevel.equals(Sword.THREE)) continue;

            boolean has = sword != null && sword.ordinal() >= swordLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + (swordLevel.ordinal() + 1) + ": §7" + swordLevel.getName()
                    + ", §b" + swordLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                team.isHighestSword()
                        ? "§cNível máximo."
                        : team.canBuySword(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((team.canBuySword(getPlayer()) ? "§a" : "§c") + "Afiação")
                .lore(lore)
                .click(event -> {
                    if (isTrioOrQuartet && sword != null && sword.equals(Sword.TWO) || team.isHighestSword()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua espada está no nível máximo.");

                        return;
                    }

                    Sword next = team.getNextSword();

                    if (!team.canBuySword(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(EggOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(EggOre.DIAMOND, next.getAdjustedPrice(arena));

                    team.getUpgrade().setSword(next);

                    // Aplica para comprador
                    user.getTeam().getSword().applyEnchantment(user.getPlayer());
                    user.getPlayer().sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu as espadas para o nível §c" + next.getTag() + "§e!");

                    // Aplica para o time inteiro
                    team.getPlayers().forEach(player -> {
                        Core.getLogger().info("Jogadores do time: " + player);
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as espadas para o nível §c" + next.getTag() + "§e!");

                        team.getSword().applyEnchantment(player);
                    });

                    handle();
                });

        return icon;
    }

    protected Item buildHaste(Team team) {
        Item icon = Item.of(Material.GOLD_PICKAXE)
                .flags(ItemFlag.values());

        Haste haste = team.getHaste();

        if (haste != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas picaretas",
                "§7mais ágeis.",
                ""
        ));

        for (Haste hasteLevel : Haste.values()) {
            boolean has = haste != null && haste.ordinal() >= hasteLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + (hasteLevel.ordinal() + 1) + ": §7" + hasteLevel.getName()
                    + ", §b" + hasteLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                team.isHighestHaste()
                        ? "§cNível máximo."
                        : team.canBuyHaste(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((team.canBuyHaste(getPlayer()) ? "§a" : "§c") + "Agilidade")
                .lore(lore)
                .click(event -> {
                    if (team.isHighestHaste()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua picareta está no nível máximo.");

                        return;
                    }

                    Haste next = team.getNextHaste();

                    if (!team.canBuyHaste(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(EggOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(EggOre.DIAMOND, next.getAdjustedPrice(arena));




                    team.getUpgrade().setHaste(next);

                    team.getPlayers().forEach(player -> {
                        team.getHaste().applyEnchantment(player);
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as picaretas para o nível §c" + next.getTag() + "§e!");
                    });

                    user.getTeam().getHaste().applyEnchantment(user.getPlayer());
                    user.getPlayer().sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu as picaretas para o nível §c" + next.getTag() + "§e!");

                });

        return icon;
    }

    protected Item buildArmor(Team team) {
        Item icon = Item.of(Material.IRON_BOOTS)
                .flags(ItemFlag.values());

        ArmorProtection armor = team.getArmor();

        if (armor != null && !icon.hasEnchantments())
            icon.enchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Torne as suas armaduras",
                "§7mais resistentes.",
                ""
        ));

        for (ArmorProtection armorLevel : ArmorProtection.values()) {
            if (armorLevel.equals(ArmorProtection.NONE)) continue;

            boolean has = armor != null && armor.ordinal() >= armorLevel.ordinal();

            lore.add((has ? "§a✓" : "§c྾") + " §7Nível " + armorLevel.ordinal() + ": §7" + armorLevel.getName()
                    + ", §b" + armorLevel.getAdjustedPrice(arena) + " diamantes");
        }

        lore.addAll(Arrays.asList(
                "",
                team.isHighestArmor()
                        ? "§cNível máximo."
                        : team.canBuyArmor(getPlayer()) ? "§eClique para evoluir!"
                        : "§cVocê não tem diamantes suficientes."
        ));

        icon.name((team.canBuyArmor(getPlayer()) ? "§a" : "§c") + "Armaduras")
                .lore(lore)
                .click(event -> {
                    if (team.isHighestArmor()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cA sua armadura está no nível máximo.");

                        return;
                    }

                    ArmorProtection next = team.getNextArmor();

                    if (!team.canBuyArmor(getPlayer())) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(EggOre.DIAMOND, next.getAdjustedPrice(arena)) + " diamantes.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    user.removeOre(EggOre.DIAMOND, next.getAdjustedPrice(arena));




                    team.getUpgrade().setArmor(next);

                    team.getPlayers().forEach(player -> {
                        player.sendMessage(team.getColor()
                                + getPlayer().getName() + "§e evoluiu as armaduras para o nível §c" + next.getTag() + "§e!");

                        // Aplicar encantamento de forma síncrona após um pequeno delay
                        Core.getPlatform().runSync(() -> {
                            team.getArmor().applyEnchantment(player);
//                            next.applyEnchantment(player);
                        }, 2L);
                    });

                    user.getTeam().getArmor().applyEnchantment(user.getPlayer());
                    user.getPlayer().sendMessage(team.getColor()
                            + getPlayer().getName() + "§e evoluiu as armaduras para o nível §c" + next.getTag() + "§e!");

                    handle();
                });

        return icon;
    }

    protected Item buildAutoRegeneration(Team team) {
        Item icon = Item.of(Material.BEACON)
                .flags(ItemFlag.values());

        TeamUpgrade upgrade = team.getUpgrade();

        final int cost = 1;
        final boolean buy = BukkitUtil.getItemAmount(getPlayer(), Material.DIAMOND) >= cost;

        if (upgrade.isAutoRegeneration())
            icon.unsafeEnchantment(Enchantment.DURABILITY, 1);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Invoque um campo de regeneração",
                "§7em volta da sua ilha.",
                ""
        ));

        if (upgrade.isAutoRegeneration())
            lore.add("§cCampo em ação.");
        else
            lore.addAll(Arrays.asList(
                    "§7Custo: §b" + cost + " diamante",
                    "",
                    buy ? "§eClique para adquirir!" : "§cVocê não tem diamantes suficientes."));

        icon.name((buy ? "§a" : "§c") + "Regeneração na ilha")
                .lore(lore)
                .click(event -> {
                    if (upgrade.isAutoRegeneration()) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cO campo de regeneração já foi ativado.");

                        return;
                    }

                    if (!buy) {
                        sound(MenuSound.ERROR);
                        getPlayer().sendMessage("§cVocê não tem diamantes suficientes. " +
                                "Faltam " + user.getRemainingOre(EggOre.DIAMOND, cost) + " diamante.");

                        return;
                    }

                    sound(MenuSound.SUCCESS);

                    upgrade.setAutoRegeneration(true);
                    team.setUpgrade(upgrade);

                    user.removeOre(EggOre.DIAMOND, cost);

                    team.getPlayers().forEach(player -> player.sendMessage(team.getColor()
                            + getPlayer().getName() + "§e ativou o campo de regeneração."));

                    handle();
                });

        return icon;
    }

    protected Item buildTrap(Team team) {
        List<String> lore = new ArrayList<>(Arrays.asList(
                "§7Use as armadilhas para",
                "§7distrair os invasores da sua ilha.",
                "",
                "§7Suas armadilhas §8(" + team.getTraps().size() + "/3)§7:"
        ));

        if (!team.getTraps().isEmpty()) {
            Queue<Trap> traps = team.getTraps();

            StringBuilder builder = new StringBuilder();

            builder.append(" ");

            boolean end = false;
            int index = 1;
            for (Trap trap : traps) {
                if (trap == null) continue;

                if (index >= traps.size()) end = true;

                builder.append("§7").append(trap.getName()).append(end ? "." : ", ");
                index++;
            }

            lore.add(builder.toString());
        } else
            lore.add("§7Não há armadilhas.");

        lore.add("");
        lore.add("§eClique para ver!");

        return new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1YmNmZjJlNzRkZWVkMzdhMzE5YTFmNDA0ZTcwZDA2YTVmMzYwY2FjZWU5OWM3MTM0NmYzODU2MGNiZDcyYSJ9fX0=")
                .name("§aArmadilhas")
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);

                    new TrapMenu(getPlayer(), this, team).handle();
                });
    }
}
