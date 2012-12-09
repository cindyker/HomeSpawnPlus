/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/**
 * @author morganm
 *
 */
public interface World {
    /**
     *  Get the name of this world.
     *  
     * @return the name of the world
     */
    public String getName();

    /**
     * Sets the spawn location of the world
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return True if it was successfully set.
     */
    public boolean setSpawnLocation(int x, int y, int z);
}
