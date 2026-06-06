package com.minecraft.proxyhost.proxy;

/**
 * Modo de bloqueio durante manutenção.
 *
 * <ul>
 *   <li>{@link #RANK} — rank mínimo configurado (padrão staff)</li>
 *   <li>{@link #WHITELIST} — lista branca + staff do rank mínimo</li>
 *   <li>{@link #FULL} — só bypass ({@code proxyhost.maintenance.bypass})</li>
 * </ul>
 */
public enum MaintenanceMode {
    RANK,
    WHITELIST,
    FULL;

    public static MaintenanceMode parse(String raw) {
        if (raw == null || raw.isBlank()) return RANK;
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return RANK;
        }
    }
}
