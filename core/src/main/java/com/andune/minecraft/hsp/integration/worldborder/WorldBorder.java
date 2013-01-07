/**
 * 
 */
package com.andune.minecraft.hsp.integration.worldborder;

import com.andune.minecraft.hsp.integration.PluginIntegration;
import com.andune.minecraft.hsp.server.api.Location;

/**
 * @author andune
 *
 */
public interface WorldBorder extends PluginIntegration {
    /**
     * Return the BorderData for a given world.
     * 
     * @param worldName
     * @return
     */
    public BorderData getBorderData(String worldName);

    public interface BorderData
    {
        public boolean insideBorder(Location l);
        public double getX();
        public double getZ();
        public int getRadius();
    }
}
