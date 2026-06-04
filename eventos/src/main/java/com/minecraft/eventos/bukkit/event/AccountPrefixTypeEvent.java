package com.minecraft.eventos.bukkit.event;

import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.account.context.objects.toggle.TagPrefix;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Espelho de {@code AccountPrefixTypeEvent} do Savage.
 *
 * <p>Disparado apos {@code Account.setTagPrefix(...)}. Listeners (ex.
 * {@code UpdateListener}) reagem chamando {@code TagManager.updateAllForViewer}.
 */
@Getter
public class AccountPrefixTypeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Account account;
    private final TagPrefix prefix;

    public AccountPrefixTypeEvent(Account account, TagPrefix prefix) {
        this.account = account;
        this.prefix = prefix;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
