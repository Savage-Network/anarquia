package com.minecraft.game.bukkit.event;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.medal.Medal;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Disparado apos {@code /medal} bem-sucedido. */
@Getter
public class AccountMedalEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Account account;
    private final Medal medal;

    public AccountMedalEvent(Account account, Medal medal) {
        this.account = account;
        this.medal = medal;
    }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
