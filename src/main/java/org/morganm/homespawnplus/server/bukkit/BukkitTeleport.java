/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.util.Random;

import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.server.api.Teleport;
import org.morganm.homespawnplus.server.api.TeleportOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitTeleport implements Teleport {
    private static final Logger log = LoggerFactory.getLogger(BukkitTeleport.class);
    
    private final Random random = new Random(System.currentTimeMillis());
    private org.morganm.mBukkitLib.Teleport teleportUtil;
    private Factory factory;
    
    @Inject
    public BukkitTeleport(org.morganm.mBukkitLib.Teleport teleportUtil, Factory factory) {
        this.teleportUtil = teleportUtil;
        this.factory = factory;
    }

    @Override
    public Location safeLocation(Location location) {
        org.bukkit.Location safeLoc = teleportUtil.safeLocation(((BukkitLocation) location).getBukkitLocation());
        return new BukkitLocation(safeLoc);
    }

    @Override
    public Location safeLocation(Location location, TeleportOptions options) {
        org.bukkit.Location safeLoc = teleportUtil.safeLocation(((BukkitLocation) location).getBukkitLocation(),
                getBounds(options), getFlags(options));
        return new BukkitLocation(safeLoc);
    }

    @Override
    public void safeTeleport(Player player, Location location) {
        ((BukkitPlayer) player).teleport(safeLocation(location));
    }

    @Override
    public void teleport(Player player, Location location, TeleportOptions options) {
        ((BukkitPlayer) player).teleport(safeLocation(location, options));
    }

    /**
     * Convert TeleportOptions into the Bounds object used by the Teleport util class.
     * 
     * @param options
     * @return
     */
    private org.morganm.mBukkitLib.Teleport.Bounds getBounds(TeleportOptions options) {
        org.morganm.mBukkitLib.Teleport.Bounds bounds = new org.morganm.mBukkitLib.Teleport.Bounds();
        bounds.minY = options.getMinY();
        bounds.maxY = options.getMaxY();
        bounds.maxRange = options.getMaxRange();
        return bounds;
    }

    /**
     * Convert TeleportOptions into flags used by the Teleport util class.
     * @param options
     * @return
     */
    private int getFlags(TeleportOptions options) {
        int flags = 0;

        if( options.isNoTeleportOverIce() )
            flags |= org.morganm.mBukkitLib.Teleport.FLAG_NO_ICE;
        if( options.isNoTeleportOverLeaves() )
            flags |= org.morganm.mBukkitLib.Teleport.FLAG_NO_LEAVES;
        if( options.isNoTeleportOverLilyPad() )
            flags |= org.morganm.mBukkitLib.Teleport.FLAG_NO_LILY_PAD;
        if( options.isNoTeleportOverWater() )
            flags |= org.morganm.mBukkitLib.Teleport.FLAG_NO_WATER;
        
        return flags;
    }

    /** Given a min and max (that define a square cube "region"), randomly pick
     * a location in between them, and then find a "safe spawn" point based on
     * that location (ie. that won't suffocate or be right above lava, etc).
     * 
     * @param min
     * @param max
     * @return the random safe Location, or null if one couldn't be located
     */
    @Override
    public Location findRandomSafeLocation(Location min, Location max, TeleportOptions options) {
        if( min == null || max == null )
            return null;
        
        if( !min.getWorld().equals(max.getWorld()) ) {
            log.warn("Attempted to find random location between two different worlds: {}, {}", min.getWorld(), max.getWorld());
            return null;
        }
        
        log.debug("findRandomSafeLocation(): min: {}, max: {}", min, max);
        
        int x = randomDeltaInt(min.getBlockX(), max.getBlockX());
        int y = randomDeltaInt(options.getMinY(), options.getMaxY());
        int z = randomDeltaInt(min.getBlockZ(), max.getBlockZ());
        
        Location newLoc = factory.newLocation(min.getWorld().getName(), x, y, z, 0, 0);
        log.debug("findRandomSafeLocation(): newLoc={}",newLoc);
        Location safeLoc = safeLocation(newLoc, options);
        log.debug("findRandomSafeLocation(): safeLoc={}",safeLoc);

        return safeLoc;
    }

    /** Given two equal integer values, figure out their "distance delta".
     * For example, assume two Locations A and B where we're trying to find
     * the distanceDelta between A.x and B.x. Here are examples:
     * 
     *   A.x: -550, B.x: -570 = 20
     *   A.x: 550, B.x: 570 = 20
     *   A.x: -50, B.x: 50 = 100
     *   
     * @param i
     * @param j
     * @return
     */
    private int getDistanceDelta(int i, int j) {
        int highest = i;
        int lowest = j;
        // swap them if it's wrong
        if( lowest > highest ) {
            highest = j;
            lowest = i;
        }
        
        // if both are < 0, swap sign and subtract lowest from highest
        // (since with swapped sign, the lowest/highest will be reversed)
        if( highest < 0 && lowest < 0 )
            return Math.abs(lowest) - Math.abs(highest);
        else
            return highest - lowest;
    }
    
    /** Given two integers representing a location component (such as x, y or z),
     * pick a random number that falls between them.
     * 
     * @param i
     * @param j
     * @return
     */
    private int randomDeltaInt(int i, int j) {
        int result = 0;
        int delta = getDistanceDelta(i, j);
        log.debug("randomDeltaInt(): i={}, j={}, delta={}", i, j, delta);
        if( delta == 0 )
            return 0;
        
        int r = random.nextInt(delta);
        if( i < j )
            result = i + r;
        else
            result = j + r;
        
        log.debug("randomDeltaInt(): i={}, j={}, delta={}, r={}, result={}", i, j, delta, r, result);
        return result;
    }
}
