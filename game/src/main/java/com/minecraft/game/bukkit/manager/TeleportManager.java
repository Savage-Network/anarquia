package com.minecraft.game.bukkit.manager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TeleportManager {

    private static final List<TeleportRequest> requests = new CopyOnWriteArrayList<>();

    private TeleportManager() {}

    public static void sendRequest(UUID sender, UUID target, TeleportRequestType type) {
        // Remove qualquer solicitação existente do mesmo remetente para o mesmo destinatário
        requests.removeIf(r -> r.getSender().equals(sender) && r.getTarget().equals(target));
        requests.add(new TeleportRequest(sender, target, type));
    }

    public static TeleportRequest getLatestRequest(UUID target) {
        cleanupExpiredRequests();
        TeleportRequest latest = null;
        for (TeleportRequest r : requests) {
            if (r.getTarget().equals(target)) {
                latest = r; // Sobrescreve para pegar o mais recente
            }
        }
        return latest;
    }

    public static TeleportRequest getRequest(UUID target, UUID sender) {
        cleanupExpiredRequests();
        for (TeleportRequest r : requests) {
            if (r.getTarget().equals(target) && r.getSender().equals(sender)) {
                return r;
            }
        }
        return null;
    }

    public static void removeRequest(TeleportRequest request) {
        if (request != null) {
            requests.remove(request);
        }
    }

    public static void cleanupExpiredRequests() {
        requests.removeIf(TeleportRequest::isExpired);
    }
}
