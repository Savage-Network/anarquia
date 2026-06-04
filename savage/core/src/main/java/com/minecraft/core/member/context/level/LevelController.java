package com.minecraft.core.member.context.level;

import com.minecraft.core.member.context.level.symbol.LevelSymbol;

import java.util.HashMap;
import java.util.Map;

public class LevelController {

    private static final Map<Integer, String> levelMap = new HashMap<>();

    static {
        levelMap.put(100, "§c[1§e0§f0§b" + LevelSymbol.TWO.getSymbol() + "§5]");
        levelMap.put(110, "§7[§f110§7" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(120, "§7[§e120§6" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(130, "§7[§b130§3" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(140, "§7[§a140§2" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(150, "§7[§3150§9" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(160, "§7[§c160§4" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(170, "§7[§d170§5" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(180, "§7[§9180§1" + LevelSymbol.TWO.getSymbol() + "§7]");
        levelMap.put(190, "§7[§5190§8" + LevelSymbol.TWO.getSymbol() + "§7]");

        levelMap.put(200, "§5[2§a0§60§e" + LevelSymbol.THREE.getSymbol() + "§2]");
        levelMap.put(210, "§6[21§f0§e" + LevelSymbol.THREE.getSymbol() + "§3]");
        levelMap.put(220, "§4[22§a0§5" + LevelSymbol.THREE.getSymbol() + "§e]");
        levelMap.put(230, "§8[2§930§4" + LevelSymbol.THREE.getSymbol() + "§f]");
        levelMap.put(240, "§b[24§30§2" + LevelSymbol.THREE.getSymbol() + "§9]");
        levelMap.put(250, "§7[25§a0§a" + LevelSymbol.THREE.getSymbol() + "§8]");
        levelMap.put(260, "§f[2§a6§50§c" + LevelSymbol.THREE.getSymbol() + "§4]");
        levelMap.put(270, "§2[2§67§30§f" + LevelSymbol.THREE.getSymbol() + "§3]");
        levelMap.put(280, "§6[2§a8§40§5" + LevelSymbol.THREE.getSymbol() + "§6]");
        levelMap.put(290, "§a[2§59§f0§2" + LevelSymbol.THREE.getSymbol() + "§a]");
        levelMap.put(300, "§e[3§a0§f0§1" + LevelSymbol.THREE.getSymbol() + "§b]");
    }

    public static String getDefaultLevelColor(int level) {
        String levelColor = levelMap.get(level);

        if (levelColor != null)
            return levelColor;

        String[] colors = {"§7", "§f", "§6", "§b", "§2", "§3", "§4", "§d", "§9", "§5"};

        int colorIndex = level / 10;
        colorIndex = Math.min(colorIndex, colors.length - 1);

        String color = colors[colorIndex];

        return color + "[" + level + LevelSymbol.ONE.getSymbol() + "]";
    }
}