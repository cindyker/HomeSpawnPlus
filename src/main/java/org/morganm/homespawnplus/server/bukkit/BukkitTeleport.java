/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Teleport;
import org.morganm.homespawnplus.server.api.TeleportOptions;

/**
 * @author morganm
 *
 */
public class BukkitTeleport implements Teleport {

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Teleport#safeLocation(org.morganm.homespawnplus.server.api.Location)
     */
    @Override
    public Location safeLocation(Location location) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Teleport#safeLocation(org.morganm.homespawnplus.server.api.Location, org.morganm.homespawnplus.server.api.TeleportOptions)
     */
    @Override
    public Location safeLocation(Location location, TeleportOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Teleport#safeTeleport(org.morganm.homespawnplus.server.api.Player, org.morganm.homespawnplus.server.api.Location)
     */
    @Override
    public void safeTeleport(Player player, Location location) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Teleport#teleport(org.morganm.homespawnplus.server.api.Player, org.morganm.homespawnplus.server.api.Location, org.morganm.homespawnplus.server.api.TeleportOptions)
     */
    @Override
    public void teleport(Player p, Location l, TeleportOptions options) {
        // TODO Auto-generated method stub

    }

}
