/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.bukkit.Bukkit;
import org.morganm.homespawnplus.server.api.Block;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.World;
import org.morganm.homespawnplus.server.api.impl.LocationAbstractImpl;

/**
 * @author morganm
 *
 */
public class BukkitLocation extends LocationAbstractImpl implements Location {
    private final org.bukkit.Location bukkitLocation;

    public BukkitLocation(org.bukkit.Location bukkitLocation) {
        this.bukkitLocation = bukkitLocation;
    }
    
    public BukkitLocation(Location l) {
        org.bukkit.World w = Bukkit.getWorld(l.getWorld().getName());
        bukkitLocation = new org.bukkit.Location(w, l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }
    
    public BukkitLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        org.bukkit.World w = Bukkit.getWorld(worldName);
        bukkitLocation = new org.bukkit.Location(w, x, y, z, yaw, pitch);
    }
    
    public org.bukkit.Location getBukkitLocation() {
        return bukkitLocation;
    }
    
    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getBlock()
     */
    @Override
    public Block getBlock() {
        return new BukkitBlock(this);
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#setX(double)
     */
    @Override
    public void setX(double x) {
        bukkitLocation.setX(x);
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getX()
     */
    @Override
    public double getX() {
        return bukkitLocation.getX();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getBlockX()
     */
    @Override
    public int getBlockX() {
        return bukkitLocation.getBlockX();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#setY(double)
     */
    @Override
    public void setY(double y) {
        bukkitLocation.setY(y);
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getY()
     */
    @Override
    public double getY() {
        return bukkitLocation.getY();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getBlockY()
     */
    @Override
    public int getBlockY() {
        return bukkitLocation.getBlockY();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#setZ(double)
     */
    @Override
    public void setZ(double z) {
        bukkitLocation.setZ(z);
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getZ()
     */
    @Override
    public double getZ() {
        return bukkitLocation.getZ();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getBlockZ()
     */
    @Override
    public int getBlockZ() {
        return bukkitLocation.getBlockZ();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#setYaw(float)
     */
    @Override
    public void setYaw(float yaw) {
        bukkitLocation.setYaw(yaw);
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getYaw()
     */
    @Override
    public float getYaw() {
        return bukkitLocation.getYaw();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#setPitch(float)
     */
    @Override
    public void setPitch(float pitch) {
        bukkitLocation.setPitch(pitch);
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#getPitch()
     */
    @Override
    public float getPitch() {
        return bukkitLocation.getPitch();
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Location#distance(org.morganm.homespawnplus.server.api.Location)
     */
    @Override
    public double distance(Location o) {
        // we can only compare distance to other BukkitLocation objects.
        if( !(o instanceof BukkitLocation) )
            throw new IllegalArgumentException("invalid object class: "+o);
            
        return bukkitLocation.distance( ((BukkitLocation) o).bukkitLocation );
    }

    @Override
    public World getWorld() {
        return new BukkitWorld(bukkitLocation.getWorld());
    }
}
