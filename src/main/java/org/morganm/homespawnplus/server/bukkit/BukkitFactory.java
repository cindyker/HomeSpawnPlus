/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.TeleportOptions;
import org.morganm.homespawnplus.strategy.StrategyContext;

/**
 * @author morganm
 *
 */
public class BukkitFactory implements Factory {

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Factory#newLocation(java.lang.String, double, double, double, float, float)
     */
    @Override
    public Location newLocation(String worldName, double x, double y, double z,
            float yaw, float pitch) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Factory#newTeleportOptions()
     */
    @Override
    public TeleportOptions newTeleportOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Factory#newStrategyContext()
     */
    @Override
    public StrategyContext newStrategyContext() {
        // TODO Auto-generated method stub
        return null;
    }

}
