package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import org.bukkit.entity.Player;

public class TitleOperator extends CollectibleOperator {

    public TitleOperator(Player host, TitleCollectible title) {
        super(host, CollectibleCategory.TITLE);

        handle(title);
    }

    @Override
    public void handle(Collectible collectible) {
        TitleCollectible title = (TitleCollectible) collectible;

        // Não mostrar título na tela, apenas ativar o colecionável
        // O AboveNameManager vai cuidar de exibir acima do nome

        setCollectible(title);
    }

    @Override
    public void cancel() {
    }
}
