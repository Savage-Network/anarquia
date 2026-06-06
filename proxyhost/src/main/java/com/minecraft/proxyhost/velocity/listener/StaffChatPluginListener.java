package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.network.StaffChatService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.nio.charset.StandardCharsets;

/** Recebe staff chat dos backends Paper via plugin messaging. */
public final class StaffChatPluginListener {

    public static final MinecraftChannelIdentifier CHANNEL =
            MinecraftChannelIdentifier.from("savagemc:staffchat");

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL)) return;
        if (!(event.getSource() instanceof ServerConnection)) return;

        byte[] data = event.getData();
        if (data == null || data.length == 0) return;

        String raw = new String(data, StandardCharsets.UTF_8);
        StaffChatService.deliverFromPayload(raw);
        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }
}
