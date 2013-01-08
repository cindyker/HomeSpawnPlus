/**
 * 
 */
package com.andune.minecraft.hsp.integration.worldguard;

import com.andune.minecraft.hsp.server.api.Location;

/**
 * @author andune
 *
 */
public interface ProtectedRegion {
    /**
     * Return the name/id of this region.
     * 
     * @return
     */
    public String getName();
    
    /**
     * Return the minimum point.
     *
     * @return
     */
    public Location getMinimumPoint();

    /**
     * Return the maximum point.
     *
     * @return
     */
    public Location getMaximumPoint();

    /**
     * Determine whether the region contains the given x,y,z point. 
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean contains(int x, int y, int z);
}
