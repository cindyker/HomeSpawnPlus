/**
 * 
 */
package org.morganm.homespawnplus.server.api;


/** API for handling Location objects.
 * 
 * @author morganm
 *
 */
public interface Location {
    /**
     * Get the World for this location
     * 
     * @return the World represented by this location
     */
    public World getWorld();

    /**
     * Gets the block at the represented location
     *
     * @return Block at the represented location
     */
    public Block getBlock();

    /**
     * Set the x-coordinate of this location
     *
     * @param x x-coordinate
     */
    public void setX(double x);

    /**
     * Gets the x-coordinate of this location
     *
     * @return x-coordinate
     */
    public double getX();

    /**
     * Gets the floored value of the X component, indicating the block that
     * this location is contained with.
     *
     * @return block X
     */
    public int getBlockX();

    /**
     * Set the y-coordinate of this location
     *
     * @param y y-coordinate
     */
    public void setY(double y);

    /**
     * Gets the y-coordinate of this location
     *
     * @return y-coordinate
     */
    public double getY();

    /**
     * Gets the floored value of the Y component, indicating the block that
     * this location is contained with.
     *
     * @return block y
     */
    public int getBlockY();

    /**
     * Set the z-coordinate of this location
     *
     * @param z z-coordinate
     */
    public void setZ(double z);

    /**
     * Gets the z-coordinate of this location
     *
     * @return z-coordinate
     */
    public double getZ();

    /**
     * Gets the floored value of the Z component, indicating the block that
     * this location is contained with.
     *
     * @return block z
     */
    public int getBlockZ();

    /**
     * Sets the yaw of this location
     *
     * @param yaw New yaw
     */
    public void setYaw(float yaw);

    /**
     * Gets the yaw of this location
     *
     * @return Yaw
     */
    public float getYaw();

    /**
     * Sets the pitch of this location
     *
     * @param pitch New pitch
     */
    public void setPitch(float pitch);

    /**
     * Gets the pitch of this location
     *
     * @return Pitch
     */
    public float getPitch();

    /**
     * Get the distance between this location and another.
     *
     * @param o The other location
     * @return the distance
     * @throws IllegalArgumentException for differing worlds
     */
    public double distance(Location o);
    
    /**
     * Return an abbreviated location string, of the form: world,x,y,z
     * Note the x,y,z will be whole integer block coordinates.
     * 
     * Example return value:  world,366,72,-244
     * 
     * @return the location string
     */
    public String shortLocationString();
}
