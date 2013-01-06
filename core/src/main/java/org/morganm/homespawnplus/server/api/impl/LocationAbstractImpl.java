/**
 * 
 */
package org.morganm.homespawnplus.server.api.impl;

import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.World;

/** Abstract Location methods that don't change between server
 * implementations
 * 
 * @author morganm
 *
 */
public abstract class LocationAbstractImpl implements Location {
    @Override
    public String shortLocationString() {
        World w = getWorld();
        String worldName = null;
        if( w != null )
            worldName = w.getName();
        else
            worldName = "(world deleted)";
        return worldName+","+getBlockX()+","+getBlockY()+","+getBlockZ();
    }
}
