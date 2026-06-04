package com.minecraft.core.bukkit.service.packet;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.service.packet.type.CommandListPacket;
import com.minecraft.core.bukkit.service.packet.type.HologramActionPacket;
import com.minecraft.core.bukkit.service.packet.type.NpcActionPacket;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class PacketHook {

    private final Plugin plugin;

    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public void handle() {
        manager.addPacketListener(new NpcActionPacket(plugin, BukkitCore.getManager().getNpc()));
        manager.addPacketListener(new HologramActionPacket(plugin, BukkitCore.getManager().getHologram()));

        manager.addPacketListener(new CommandListPacket(plugin));
    }
}
