package com.minecraft.arcade.eggwars.user;

import com.minecraft.arcade.eggwars.arcade.Arcade;
import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.structure.team.Team;
import com.minecraft.arcade.eggwars.user.context.UserContext;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.member.list.eggwars.EggMember;
import com.minecraft.core.member.list.eggwars.objects.enums.EggOre;
import com.minecraft.core.member.list.eggwars.objects.enums.EggShop;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
import com.minecraft.core.member.list.eggwars.objects.metadata.objects.stats.EggStats;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class User extends UserModel {

    private final EggMember member;
    private UserContext context;

    private Arena arena;
    private Team team;

    private int respawnTime;

    public User(EggMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;

        this.context = new UserContext();
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        setSidebar(new Sidebar(player, "§6§lEGG WARS"));

        if (arena != null)
            arena.join(player);
    }

    public void reset() {
        setContext(new UserContext());
    }

    public Player getPlayer() {
        return getAccount().player();
    }

    public Arcade getArcade() {
        return arena != null ? arena.getArcade() : null;
    }

    @Override
    public void setState(ArcadeState state) {
        super.setState(state);

        Player player = getPlayer();

        Core.getPlatform().runSync(() -> {
            switch (state) {
                case RESPAWN: {
                    player.getInventory().clear();
                    // Remove all potion effects
                    for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    player.setHealth(player.getMaxHealth());

                    player.setFlying(true);

                    player.addPotionEffects(Arrays.asList(
                            new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                            new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3)));

                    player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);

                    this.respawnTime = 5;
                    break;
                }

                case DEAD: {
                    // Remove all potion effects
                    for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(true);

                    player.addPotionEffects(Arrays.asList(
                            new PotionEffect(PotionEffectType.BLINDNESS, 45, 3),
                            new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3)));

                    player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1.0f, 1.0f);
                    break;
                }

                default: {
                    context.setProtectionTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1) + TimeUnit.MILLISECONDS.toMillis(5));

                    // Remove all potion effects
                    for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    player.setHealth(player.getMaxHealth());
                    player.setFlying(false);
                    break;
                }
            }
        });
    }

    public boolean isProtected() {
        if (!isPlayer() || !inState(ArcadeState.ALIVE)) return true;

        if (context.getProtectionTime() > -1L && context.getProtectionTime() > System.currentTimeMillis()) return true;

        return arena == null || !arena.isPhase(RoomPhase.PLAYING);
    }

    public boolean isImmuneTraps() {
        return context.getMilkTime() > System.currentTimeMillis();
    }

    public int getXpGain() {
        return context.getXpGain();
    }

    public void setXpGain(int xpGain) {
        context.setXpGain(getXpGain() + xpGain);
    }

    /* Stats */
    public int getKills() {
        return context.getKills();
    }

    public int getDeaths() {
        return context.getDeaths();
    }

    public int getFinalKill() {
        return context.getFinalKill();
    }

    public int getEggDestruction() {
        return context.getEggDestruction();
    }

    public void setKills() {
        context.setKills(getKills() + 1);

        EggStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setKills();

            member.updateStats(stats);
        }
    }

    public void setDeaths() {
        context.setDeaths(getDeaths() + 1);

        EggStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setDeaths();

            member.updateStats(stats);
        }
    }

    public void setMatches() {
        EggStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setMatches();

            member.updateStats(stats);
        }
    }

    public void setFinalKill() {
        context.setFinalKill(getFinalKill() + 1);

        EggStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setFinalKill();

            member.updateStats(stats);
        }
    }

    public void setEggDestruction() {
        context.setEggDestruction(getEggDestruction() + 1);

        EggStats stats = member.getStats(getArcade().getCategory(), arena.getType());

        if (stats != null) {
            stats.setEggDestruction();

            member.updateStats(stats);
        }
    }

    /* Item Methods */
    public boolean canBuyItem(EggWarsItem item) {
        return BukkitUtil.getItemAmount(getPlayer(), item.getOre().getMaterial()) >= item.getPrice();
    }

    public int getRemainingOre(EggWarsItem item) {
        return item.getPrice() - BukkitUtil.getItemAmount(getPlayer(), item.getOre().getMaterial());
    }

    public void removeOre(EggWarsItem item) {
        BukkitUtil.removeItemByAmount(getPlayer(), item.getOre().getMaterial(), item.getPrice());
    }

    public int getRemainingOre(EggOre ore, int price) {
        return price - BukkitUtil.getItemAmount(getPlayer(), ore.getMaterial());
    }

    public void removeOre(EggOre ore, int price) {
        BukkitUtil.removeItemByAmount(getPlayer(), ore.getMaterial(), price);
    }

    public boolean hasTool(EggWarsItem item) {
        if (!item.isType(EggShop.TOOLS)) return false;

        EggWarsItem pickaxe = context.getPickaxeItem(),
                axe = context.getAxeItem();

        return item.isToolPickaxe() && item.isInferior(pickaxe) || item.isToolAxe() && item.isInferior(axe);
    }

    public boolean hasArmor(EggWarsItem item) {
        if (!item.isType(EggShop.ARMOR)) return false;

        EggWarsItem armor = context.getArmorItem();

        return armor != null && armor.isType(EggShop.ARMOR) && armor.ordinal() >= item.ordinal();
    }
}
