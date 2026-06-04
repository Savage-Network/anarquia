package com.minecraft.arcade.eggwars.user.context;

import com.minecraft.arcade.eggwars.user.context.objects.tracker.UserTracker;
import com.minecraft.core.api.reconnect.Reconnect;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class UserContext {

    private UserTracker tracker = new UserTracker();

    private EggWarsItem pickaxeItem, axeItem, armorItem;
    private Reconnect reconnect;

    private int xpGain, kills, deaths, finalKill, eggDestruction;

    private boolean purchasedShears, purchasedPortableShop;

    private long milkTime = -1L, protectionTime = -1L;

    public boolean hasReconnect() {
        return reconnect != null;
    }

    public void downGradePickaxe(Player player) {
        if (pickaxeItem == null || pickaxeItem.equals(EggWarsItem.TOOLS_WOOD_PICKAXE)) return;

        EggWarsItem previousPick = EggWarsItem.of(item -> item.getStack().getType().name().endsWith("_PICKAXE")
                && item.ordinal() == (pickaxeItem.ordinal() - 1));

        BukkitUtil.replaceItemByType(player, pickaxeItem.getStack().getType(), previousPick.getStack());

        this.pickaxeItem = previousPick;
    }

    public void downGradeAxe(Player player) {
        if (axeItem == null || axeItem.equals(EggWarsItem.TOOLS_WOOD_AXE)) return;

        EggWarsItem previousAxe = EggWarsItem.of(item -> item.getStack().getType().name().endsWith("_AXE")
                && item.ordinal() == (axeItem.ordinal() - 1));

        BukkitUtil.replaceItemByType(player, axeItem.getStack().getType(), previousAxe.getStack());

        this.axeItem = previousAxe;
    }
}
