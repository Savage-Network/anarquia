package com.minecraft.core.bukkit.server;

import com.minecraft.core.backend.data.list.server.ServerData;
import org.bukkit.Bukkit;

public class BukkitServer extends ServerData {

    @Override
    public int getPort() {
        return Bukkit.getPort();
    }
}
