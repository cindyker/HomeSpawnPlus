/**
 * 
 */
package org.morganm.homespawnplus.server.api;

import org.morganm.homespawnplus.strategy.StrategyContext;

/** Factory for creating various implementation-specific objects.
 * 
 * @author morganm
 *
 */
public interface Factory {
    /**
     * Factory method for creating a new Location object
     * 
     * @param worldName the name of the world the Location is on
     * @param x the x coordinates
     * @param y the y coordinates
     * @param z the z coordinates
     * @param yaw the yaw (360-degree horizontal view angle)
     * @param pitch the pitch (360-degree verticle view angle)
     * 
     * @return the new Location object
     */
    public Location newLocation(String worldName, double x, double y, double z, float yaw, float pitch);
    
    /**
     * Factory method for creating a new TeleportOptions object
     * 
     * @return the new TeleportOptions object
     */
    public TeleportOptions newTeleportOptions();
    
    /**
     * Factory method for creating a new StrategyContext object
     * 
     * @return the new StrategyContext object
     */
    public StrategyContext newStrategyContext();
}
