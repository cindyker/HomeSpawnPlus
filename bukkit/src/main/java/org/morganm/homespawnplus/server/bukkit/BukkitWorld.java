/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.World;

/**
 *  API implementation for a Bukkit world. Essentially a proxy object.
 * 
 * @author morganm
 *
 */
public class BukkitWorld implements World {
    final org.bukkit.World bukkitWorld;
    
    public BukkitWorld(org.bukkit.World world) {
        bukkitWorld = world;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.World#getName()
     */
    @Override
    public String getName() {
        return bukkitWorld.getName();
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        return bukkitWorld.setSpawnLocation(x,  y,  z);
    }

    @Override
    public Location getSpawnLocation() {
        return new BukkitLocation(bukkitWorld.getSpawnLocation());
    }
    
    public org.bukkit.World getBukkitWorld() {
        return bukkitWorld;
    }
}
