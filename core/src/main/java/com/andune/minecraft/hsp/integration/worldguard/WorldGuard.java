/**
 * 
 */
package com.andune.minecraft.hsp.integration.worldguard;

import com.andune.minecraft.hsp.integration.PluginIntegration;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.World;

/**
 * @author andune
 *
 */
public interface WorldGuard extends PluginIntegration {
    /**
     * Return a ProtectedRegion for a given world & region.
     * 
     * @param worldName
     * @param regionName
     * @return
     */
    public ProtectedRegion getProtectedRegion(World world, String regionName);
    
    /**
     * Given a location, determine if it is within any WorldGuard regions
     * and if so, if said region has the spawn flag set. If it does, then
     * return the defined spawn point.
     * 
     * @param location
     * @return
     */
    public Location getWorldGuardSpawnLocation(Location location);

    /**
     * Register interest in a specific region. This makes sure any underlying
     * server hooks are in place to respond to events for the region.
     * 
     * @param world
     * @param regionName
     */
    public void registerRegion(World world, String regionName);
    
    /**
     * Determine if a given location is located within a given region by name.
     * 
     * @param l
     * @param regionName
     * @return
     */
    public boolean isLocationInRegion(Location l, String regionName);
}
