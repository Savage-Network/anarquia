package com.minecraft.game.report;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.NetworkPresenceData;
import com.minecraft.game.bukkit.vanish.VanishService;
import com.minecraft.game.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Staff entra em modo espectador invisivel perto do alvo da denuncia. */
public final class ReportSpectatorService {

    private static final Map<UUID, Long> ACTIVE = new ConcurrentHashMap<>();
    private static final long DURATION_MS = 120_000L;

    private ReportSpectatorService() {}

    public static boolean start(Player staff, String targetName) {
        if (staff == null || targetName == null || targetName.isBlank()) return false;
        Account acc = Core.getAccountController().of(staff.getUniqueId());
        if (acc == null || !acc.isStaffer()) return false;

        Optional<NetworkPresenceData.Presence> presence =
                Core.getNetworkPresenceData().findOnlineByName(targetName.trim());
        if (presence.isEmpty()) {
            staff.sendMessage(ModernText.parse("<!italic><red>Alvo offline ou não encontrado na rede."));
            return false;
        }

        if (!"game".equalsIgnoreCase(presence.get().serverId())) {
            staff.sendMessage(ModernText.parse("<!italic><yellow>Alvo em outro servidor ("
                    + presence.get().serverId() + "). Use /go " + targetName + "."));
            return false;
        }

        Player target = Bukkit.getPlayer(presence.get().playerId());
        if (target == null || !target.isOnline()) {
            staff.sendMessage(ModernText.parse("<!italic><red>Jogador não está online aqui."));
            return false;
        }

        if (!VanishService.isVanished(staff)) {
            VanishService.setVanished(staff, true);
        }
        staff.teleport(target.getLocation());
        ACTIVE.put(staff.getUniqueId(), System.currentTimeMillis() + DURATION_MS);
        staff.sendMessage(ModernText.parse("<!italic><green>Modo espectador por 2 minutos. Alvo: "
                + target.getName()));

        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> end(staff), 20L * 120L);
        return true;
    }

    public static void end(Player staff) {
        if (staff == null) return;
        ACTIVE.remove(staff.getUniqueId());
        staff.sendMessage(ModernText.parse("<!italic><gray>Modo espectador encerrado."));
    }

    public static boolean isActive(UUID staffId) {
        Long until = ACTIVE.get(staffId);
        return until != null && until > System.currentTimeMillis();
    }
}
