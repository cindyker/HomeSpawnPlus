/**
 * 
 */
package org.morganm.homespawnplus.server.api.impl;

import org.bukkit.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Teleport;
import org.morganm.homespawnplus.server.api.TeleportOptions;

/** Class that implements parts of the Teleport API that are
 * server-agnostic.
 * 
 * @author morganm
 *
 */
public abstract class TeleportAbstractImpl implements Teleport {

    @Override
    public Location safeLocation(Location location) {
        return safeLocation(location, null);
    }

    @Override
    public Location safeLocation(Location location, TeleportOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void safeTeleport(Player player, Location location) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Teleport#teleport(org.morganm.homespawnplus.server.api.Player, org.bukkit.Location, org.morganm.homespawnplus.server.api.TeleportOptions)
     */
    @Override
    public void teleport(Player p, Location l, TeleportOptions options) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Teleport#findRandomSafeLocation(org.bukkit.Location, org.bukkit.Location, org.morganm.homespawnplus.server.api.TeleportOptions)
     */
    @Override
    public Location findRandomSafeLocation(Location min, Location max,
            TeleportOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

}
