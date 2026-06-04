package com.minecraft.core.bukkit.api.npc.type.server;

import com.minecraft.core.bukkit.api.npc.Npc;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NpcServer extends Npc {
    
    public NpcServer(Location location) {
        this(location, null);
    }

    public NpcServer(Location location, Property textures) {
        super(location);

        if (textures != null)
            updateTexture(textures);
    }

    @Override
    public void display() {
        super.display();

        Bukkit.getOnlinePlayers().forEach(this::spawnTo);
    }

    @Override
    public void destroy() {
        super.destroy();

        Bukkit.getOnlinePlayers().forEach(this::despawnTo);
    }
}
