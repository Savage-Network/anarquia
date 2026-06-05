package com.minecraft.game.network;

/** Online total da rede (proxy), atualizado via Redis. */
public final class NetworkOnlineCache {

    private static volatile int networkOnline;

    private NetworkOnlineCache() {}

    public static int get() {
        return networkOnline;
    }

    public static void set(int count) {
        networkOnline = Math.max(0, count);
    }
}
