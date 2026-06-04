package com.minecraft.core.bukkit.menu.account.pluscolor;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.manager.list.TagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmberPlusColorMenu extends Menu {

    // Ícone do botão "Cores do +" (base64 enviado por você)
    public static final String MENU_ICON_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg0OWNhZWI4ZDUxNDFmMmFiY2EzNDgyMWQ0MDE1NDVjNWJiOTAwOWQ2NjhjOTNmMzE1MmQwOWRiNDllNjIzIn19fQ==";

    // Cabeças 1:1 por cor (textures base64)
    private static final String HEAD_DARK_RED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhkNDA5MzUyNzk3NzFhZGM2MzkzNmVkOWM4NDYzYWJkZjVjNWJhNzhkMmU4NmNiMWVjMTBiNGQxZDIyNWZiIn19fQ==";
    private static final String HEAD_RED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNmNzliMjA3ZDYxZTEyMjUyM2I4M2Q2MTUwOGQ5OWNmYTA3OWQ0NWJmMjNkZjJhOWE1MTI3ZjkwNzFkNGIwMCJ9fX0=";
    private static final String HEAD_GOLD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjA5MGQwOWUxNzNlZTM0MTM4YzNiMDFiNDhlZTBiZTUzNGJiYjFhY2UwZGRmNWZmOThlNjZmN2IwMjExMzk5NSJ9fX0=";
    private static final String HEAD_YELLOW = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg5YjkzZmQ2MTZlZDM2NzBjY2Y2NDdhMGY5MzgwMzk4YzBkNDYxNTYzNGYyZGVmZjQ2YzZlZGJkYzcxMjg4NSJ9fX0=";
    private static final String HEAD_GREEN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTI1YjhlZWQ1YzU2NWJkNDQwZWM0N2M3OWMyMGQ1Y2YzNzAxNjJiMWQ5YjVkZDMxMDBlZDYyODNmZTAxZDZlIn19fQ==";
    private static final String HEAD_DARK_GREEN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWUzOGZhMzEzMWUyZGEwNGE4ZjhkNTA4NzJhODIzMmQ3ZDlkZWEzNDBkMDZjOTA5N2ZmYTNjYzQ4MjA4ZGYxZCJ9fX0=";
    private static final String HEAD_AQUA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ2YThiNDdkYTkyM2I3ZDEwMTQyNDQ3ZmRiZGNmZDFlOGU4MmViNDg0OTY0MjUyYmIzNmRkYjVmNzNiNTFjMiJ9fX0=";
    private static final String HEAD_DARK_AQUA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFmNTcwNTExMzBlODUwODQ4ZThlMzdlNzIxMTBhMTZmMDlkYmRhYjdkOWQ2ZTMzYTlmZWNmZDM0OGQ1YTExMCJ9fX0=";
    private static final String HEAD_BLUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y5YzMyMTM4Yzk3NjRjNjM5YWViZDgxOWNkOTE5OTJhZWQwMWJmNDQ4ZjBlNzEwYTAzYWI0NDNhYzQ5MGVlOSJ9fX0=";
    private static final String HEAD_DARK_BLUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTExOTRmZTllZGY1ODNjMGViZTdkYzFkMzQzMDliZWVmYjIyOWJiMTViNmE4YzNjN2IwYzc2ZGUyN2M4YjdiZiJ9fX0=";
    private static final String HEAD_PINK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNmMjgzNTE4MGNiZmVjM2IzMTdkNmE0NzQ5MWE3NGFlNzE0MzViYTE2OWE1NzkyNWI5MDk2ZWEyZjljNjFiNiJ9fX0=";
    private static final String HEAD_PURPLE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI4MmU3MmI4ZTQ4MzJlNWExMTRhYjBmYzEyN2M4YWNiODNmMzFmZDRkMjY2ZDA4YjJjYWNjNWI2NDAxYTQwMCJ9fX0=";
    private static final String HEAD_WHITE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM1MzE3YTk1NjcwM2Y5MjllZmQyMDEzNTY1Njc1ZmVjY2I2MGJjY2QxYzZlNWE4YWQ0YjkxNTUzOWI3YTU0ZiJ9fX0=";
    private static final String HEAD_GRAY = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM4ZTBkZGYyNDMyZjQzMzJiODc2OTFiNTk1MmM3Njc5NzYzZWY0ZjI3NWI4NzRlOWJjZWI4ODhlZDViNWI5In19fQ==";
    private static final String HEAD_DARK_GRAY = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY5YmI5ZTU2MTI1YzgyMjdiOTRiYmRhOWY2ZTBmODYyOTMxYzIyOTI1NWJhOGYxMjA1ZDEzYzQ0YzFiYjU2MSJ9fX0=";

    private static final Map<String, ColorOption> COLORS = new LinkedHashMap<>();
    static {
        COLORS.put("§4Vermelho escuro", new ColorOption("§4", HEAD_DARK_RED));
        COLORS.put("§cVermelho", new ColorOption("§c", HEAD_RED));
        COLORS.put("§6Dourado", new ColorOption("§6", HEAD_GOLD));
        COLORS.put("§eAmarelo", new ColorOption("§e", HEAD_YELLOW));
        COLORS.put("§aVerde", new ColorOption("§a", HEAD_GREEN));
        COLORS.put("§2Verde escuro", new ColorOption("§2", HEAD_DARK_GREEN));
        COLORS.put("§bCiano", new ColorOption("§b", HEAD_AQUA));
        COLORS.put("§3Ciano escuro", new ColorOption("§3", HEAD_DARK_AQUA));
        COLORS.put("§9Azul", new ColorOption("§9", HEAD_BLUE));
        COLORS.put("§1Azul escuro", new ColorOption("§1", HEAD_DARK_BLUE));
        COLORS.put("§dRosa", new ColorOption("§d", HEAD_PINK));
        COLORS.put("§5Roxo (padrão)", new ColorOption("§5", HEAD_PURPLE));
        COLORS.put("§fBranco", new ColorOption("§f", HEAD_WHITE));
        COLORS.put("§7Cinza", new ColorOption("§7", HEAD_GRAY));
        COLORS.put("§8Cinza escuro", new ColorOption("§8", HEAD_DARK_GRAY));
    }

    private static final class ColorOption {
        final String colorCode;
        final String headBase64;

        private ColorOption(String colorCode, String headBase64) {
            this.colorCode = colorCode;
            this.headBase64 = headBase64;
        }
    }

    private final Account account;

    public EmberPlusColorMenu(Player player, Menu last) {
        super(player, "Cores do + (EMBER+)", last, 3);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        if (account == null || !account.hasRank(RankType.EMBER_PLUS)) {
            sound(MenuSound.ERROR);
            if (isReturnable()) addBackButton();
            display();
            return;
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int i = 0;

        for (Map.Entry<String, ColorOption> entry : COLORS.entrySet()) {
            if (i >= slots.length) break;

            String display = entry.getKey();
            ColorOption option = entry.getValue();
            String colorCode = option.colorCode;

            addItem(slots[i], Item.of(Material.SKULL_ITEM, 3, "§a" + display,
                            "§7Cor atual: " + account.getEmberPlusColor() + "§l+",
                            "§7Exemplo: §6§lEMBER" + colorCode + "§l+",
                            "",
                            "§eClique para escolher!")
                    .skullByBase64(option.headBase64)
                    .click(e -> {
                        account.setEmberPlusColor(colorCode);
                        TagManager.updateTag(account);
                        sound(MenuSound.SUCCESS);
                        account.send("§aCor do + do EMBER+ alterada para: " + colorCode + "§l+");
                        handle();
                    }));

            i++;
        }

        if (isReturnable()) addBackButton();
        display();
    }
}

