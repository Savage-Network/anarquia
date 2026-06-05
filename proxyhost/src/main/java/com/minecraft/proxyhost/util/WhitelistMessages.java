package com.minecraft.proxyhost.util;

import com.minecraft.proxyhost.ProxySettings;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.whitelist.WhitelistRule;
import com.minecraft.proxyhost.whitelist.WhitelistTarget;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/** Mensagens formatadas da whitelist (chat e desconexão). */
public final class WhitelistMessages {

    private WhitelistMessages() {}

    public static Component chatDenied(WhitelistTarget target, WhitelistRule rule) {
        List<String> lines = new ArrayList<>();
        lines.add(DisconnectMessages.SEPARATOR);
        lines.add("§6§lSavageMC §8» §c§lWHITELIST");
        lines.add(" ");
        lines.add("§7O modo §f" + target.displayName() + " §7está em whitelist.");
        lines.add("§7Rank mínimo: §e" + rule.getMinRank().name());
        if (!rule.getExtraRanks().isEmpty()) {
            lines.add("§7Ranks autorizados: §f" + formatRanks(rule));
        }
        lines.add(" ");
        lines.add("§7Apenas jogadores autorizados podem entrar.");
        lines.add("§7Site: §6" + ProxySettings.SERVER_ADDRESS);
        lines.add(DisconnectMessages.SEPARATOR);
        return LegacyTexts.joinLines(LegacyTexts.colorize(lines));
    }

    public static Component loginDenied(WhitelistTarget target, WhitelistRule rule) {
        List<String> lines = new ArrayList<>();
        lines.add(DisconnectMessages.BRAND);
        lines.add("§7" + ProxySettings.SERVER_ADDRESS);
        lines.add(" ");
        lines.add("§c§lWHITELIST ATIVA");
        lines.add(" ");
        if (target == WhitelistTarget.PROXY) {
            lines.add("§7A rede SavageMC está em whitelist.");
        } else {
            lines.add("§7O modo §f" + target.displayName() + " §7está em whitelist.");
        }
        lines.add("§7Rank mínimo: §e" + rule.getMinRank().name());
        if (!rule.getExtraRanks().isEmpty()) {
            lines.add("§7Ranks extras: §f" + formatRanks(rule));
        }
        lines.add(" ");
        lines.add("§7Se acha que deveria ter acesso, contate a equipe.");
        lines.add("§7Loja: §6" + ProxySettings.SERVER_STORE_URL);
        lines.add(DisconnectMessages.SEPARATOR);
        return LegacyTexts.joinLines(LegacyTexts.colorize(lines));
    }

    private static String formatRanks(WhitelistRule rule) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (RankType rank : rule.getExtraRanks()) {
            if (i++ > 0) sb.append("§7, ");
            sb.append("§f").append(rank.name());
        }
        return sb.toString();
    }
}
