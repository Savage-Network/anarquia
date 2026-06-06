package com.minecraft.eventos.service;

import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.config.EventConfig;
import com.minecraft.eventos.engine.EventSession;
import com.minecraft.eventos.util.AccountSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Distribui golds ao fim do evento. */
public final class EventRewardService {

    private EventRewardService() {}

    public static void distribute(EventSession session) {
        if (session == null) return;
        int winnerGold = EventConfig.getWinnerGold();
        int participantGold = EventConfig.getParticipantGold();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Account account = com.minecraft.eventos.Core.getAccountController().of(p.getUniqueId());
            if (account == null) continue;
            if (!session.getContext().isParticipant(p.getUniqueId())) continue;

            int gold = participantGold;
            if (session.getWinnerId() != null && session.getWinnerId().equals(p.getUniqueId())) {
                gold = winnerGold;
            }
            if (gold <= 0) continue;

            account.addGolds(gold);
            AccountSync.saveAndBroadcast(account);
            p.sendMessage(com.minecraft.eventos.message.EventMessages.component(
                    "event.reward.received",
                    java.util.Map.of("gold", String.valueOf(gold))));
        }
    }
}
