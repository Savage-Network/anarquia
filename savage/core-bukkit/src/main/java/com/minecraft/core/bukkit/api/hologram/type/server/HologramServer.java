package com.minecraft.core.bukkit.api.hologram.type.server;

import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.row.HologramRow;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.List;

public class HologramServer extends Hologram {

    public HologramServer(String tag, Location location) {
        super(tag, location);
    }

    public HologramServer(String tag, Location location, long expiresAt) {
        super(tag, location, expiresAt);
    }

    @Override
    public void teleport(Location location) {
        super.teleport(location);

        getRows().forEach(row -> getViewers().forEach(row::respawn));
    }

    @Override
    public synchronized void setText(List<String> lines) {
        final Location origin = getLocation();

        int lastIndex = getRows().size() - 1;

        if (getRows().isEmpty()) {
            Location clone = origin.clone();

            clone.setY(clone.getY() + 0.5);

            double adjustment = lines.size() > 2 ? (lines.size() - 1.5) * 0.33 : 0;
            clone.setY(clone.getY() + adjustment);

            for (String line : lines) {
                clone.setY(clone.getY() - 0.35);

                getRows().add(new HologramRow(this, clone, line));
            }

            getRows().forEach(row -> getViewers().forEach(row::spawn));
            return;
        }

        int diff = lines.size() - getRows().size();

        if (diff > 0) { // Adicionar novas linhas
            int index = 0;

            for (HologramRow row : getRows()) {
                row.setText(lines.get(index++));
            }

            updateRows();

            HologramRow lastRow = getRows().get(lastIndex);
            Location loc = lastRow.getLocation().clone().subtract(0, 0.25, 0);

            while (diff > 0) {
                HologramRow row = new HologramRow(this, loc, lines.get(index++));

                getRows().add(row);
                getViewers().forEach(row::spawn);


                loc = loc.clone().subtract(0, 0.25, 0);
                diff--;
            }
        } else { // Remove linhas e atualiza as restantes
            Iterator<HologramRow> rowIterator = getRows().iterator();

            for (int i = 0; rowIterator.hasNext(); i++) {
                HologramRow row = rowIterator.next();

                if (i < lines.size()) {
                    row.setText(lines.get(i));
                } else {
                    getViewers().forEach(row::despawn);
                    rowIterator.remove();
                }
            }

            updateRows();
        }
    }


    @Override
    public void updateRows() {
        getRows().forEach(row -> getViewers().forEach(row::update));
    }
}
