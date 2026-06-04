package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.aura.AuraCollectible;
import com.minecraft.core.api.collectible.type.aura.image.ImageAuraCollectible;
import com.minecraft.core.api.collectible.type.aura.image.ImageAuraList;
import com.minecraft.core.api.collectible.type.aura.AuraList;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ImageAuraSelectorMenu extends Menu {

    private final Account account;
    public ImageAuraSelectorMenu(Player player, Menu last) {
        super(player, "Auras", last, 6);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        addItem(4, Item.of(Material.BEACON)
                .name("§6Auras de Imagem")
                .lore(
                        "§7Imagens formadas em partículas",
                        "§7coloridas acima da sua cabeça.",
                        "",
                        "§7Personalizada: §f/aura link <imgur>",
                        "",
                        account.getImageAuraMetadata().hasImage()
                                ? "§7Seu link: §f" + shorten(account.getImageAuraMetadata().getImageUrl())
                                : "§7Nenhum link configurado."
                ));

        int slot = 19;
        for (ImageAuraList preset : ImageAuraList.values()) {
            ImageAuraCollectible aura = preset.getCosmetic();
            boolean using = account.isUsingCollectible(aura);
            boolean available = aura.hasAccess(account.getRank().getType());

            List<String> lore = new ArrayList<>(aura.getLore());
            if (aura.isUsesAccountUrl()) {
                lore.add("");
                lore.add("§7Use §f/aura link <url> §7no chat.");
            }
            lore.add("");
            lore.add(using ? "§cJá selecionada." : available ? "§eClique para selecionar!" : "§cIndisponível.");

            addItem(slot++, (available ? aura.getIcon() : Item.of(Material.INK_SACK, 8))
                    .name((using ? "§e" : available ? "§a" : "§c") + aura.getName())
                    .lore(lore)
                    .click(event -> selectAura(aura, using, available)));
            if (slot == 26) slot = 28;
        }

        addItem(39, Item.of(Material.BLAZE_POWDER)
                .name("§7Auras clássicas (anel)")
                .lore("§7Partículas ao redor do corpo.", "§8Selecione abaixo:"));

        int effectSlot = 40;
        for (AuraList entry : AuraList.values()) {
            AuraCollectible aura = entry.getCosmetic();
            boolean using = account.isUsingCollectible(aura);
            boolean available = aura.hasAccess(account.getRank().getType());
            Item icon = available ? aura.getIcon() : Item.of(Material.INK_SACK, 8);
            addItem(effectSlot++, icon
                    .name((using ? "§e" : available ? "§a" : "§c") + aura.getName())
                    .lore(aura.getLore())
                    .click(event -> selectAura(aura, using, available)));
        }

        CollectibleOperator operator = CollectibleOperator.of(getPlayer(), CollectibleCategory.AURA);
        if (operator != null) {
            addItem(49, Item.of(Material.BARRIER, "§cRemover aura")
                    .click(event -> {
                        close();
                        sound(MenuSound.PAGINATED);
                        CollectibleOperator.unload(getPlayer(), CollectibleCategory.AURA);
                        account.send("§eSua aura foi removida.");
                    }));
        }

        addBackButton(45);
        addCloseButton(53);
        display();
    }

    private void selectAura(com.minecraft.core.api.collectible.Collectible aura, boolean using, boolean available) {
        if (!available) {
            sound(MenuSound.ERROR);
            account.send("§cVocê não possui " + aura.getName() + ".");
            return;
        }
        if (using) {
            sound(MenuSound.ERROR);
            account.send("§cO " + aura.getName() + " já foi selecionado.");
            return;
        }
        if (aura instanceof ImageAuraCollectible && ((ImageAuraCollectible) aura).isUsesAccountUrl()
                && !account.getImageAuraMetadata().hasImage()) {
            sound(MenuSound.ERROR);
            account.send("§cConfigure com §f/aura link <url do Imgur>§c.");
            return;
        }

        close();
        sound(MenuSound.SUCCESS);
        CollectibleOperator.unloadConflicts(getPlayer(), CollectibleCategory.AURA);
        CollectibleOperator.unload(getPlayer(), CollectibleCategory.AURA);
        CollectibleOperator.start(getPlayer(), aura);
        account.send("§bVocê selecionou §e" + aura.getName() + " §bcomo sua aura.");
    }

    private String shorten(String url) {
        if (url == null) return "";
        return url.length() > 36 ? url.substring(0, 33) + "..." : url;
    }
}
