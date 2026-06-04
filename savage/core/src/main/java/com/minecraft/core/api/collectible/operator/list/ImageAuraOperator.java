package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.aura.image.ImageAuraCollectible;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class ImageAuraOperator extends CollectibleOperator {

    public ImageAuraOperator(Player host, ImageAuraCollectible aura) {
        super(host, CollectibleCategory.AURA);
        handle(aura);
    }

    @Override
    public void handle(Collectible collectible) {
        ImageAuraCollectible aura = (ImageAuraCollectible) collectible;
        Player player = getHost();
        if (player == null || !player.isOnline())
            return;

        String resourcePath = aura.getResourcePath();
        String imageUrl = aura.getImageUrl();

        if (aura.isUsesAccountUrl()) {
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account == null) {
                player.sendMessage("§cNão foi possível carregar sua conta.");
                return;
            }
            if (!account.getImageAuraMetadata().hasImage()) {
                account.send("§cDefina sua imagem com §f/aura link <url do Imgur>§c.");
                return;
            }
            imageUrl = account.getImageAuraMetadata().getImageUrl();
            resourcePath = null;
        }

        try {
            Class<?> engineClass = Class.forName("com.minecraft.core.bukkit.api.shape.manager.ImageAuraEngine");
            Object engine = engineClass.getMethod("getInstance").invoke(null);

            if (resourcePath != null && !resourcePath.isEmpty()) {
                Method start = engineClass.getMethod("startFromResource", Player.class, String.class, float.class, int.class);
                start.invoke(engine, player, resourcePath, aura.getRatio(), aura.getScanQuality());
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                Method start = engineClass.getMethod("startFromUrl", Player.class, String.class, float.class, int.class);
                start.invoke(engine, player, imageUrl, aura.getRatio(), aura.getScanQuality());
            } else {
                player.sendMessage("§cEsta aura não possui imagem configurada.");
                return;
            }

            setCollectible(aura);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cNão foi possível carregar a aura de imagem.");
        }
    }

    @Override
    public void cancel() {
        Player player = getHost();
        if (player == null)
            return;
        try {
            Class<?> engineClass = Class.forName("com.minecraft.core.bukkit.api.shape.manager.ImageAuraEngine");
            Object engine = engineClass.getMethod("getInstance").invoke(null);
            engineClass.getMethod("stop", Player.class).invoke(engine, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
