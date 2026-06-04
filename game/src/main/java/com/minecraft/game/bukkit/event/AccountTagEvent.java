package com.minecraft.game.bukkit.event;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.tag.Tag;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Disparado apos {@code /tag} bem-sucedido. */
@Getter
public class AccountTagEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Account account;
    private final Tag tag;

    public AccountTagEvent(Account account, Tag tag) {
        this.account = account;
        this.tag = tag;
    }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
