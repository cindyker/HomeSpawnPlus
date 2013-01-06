/**
 * 
 */
package com.andune.minecraft.hsp.config;


/**
 * @author morganm
 *
 */
public abstract class PerWorldEntry extends PerXEntry {
    protected String world;

    /**
     * Get the world that this entry is for.
     * 
     * @return
     */
    public String getWorld() {
        return world;
    }
    
    /**
     * Set the world for this entry.
     * 
     * @param world
     */
    void setWorld(String world) {
        this.world = world;
    }
}
