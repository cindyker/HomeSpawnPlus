/**
 * 
 */
package com.andune.minecraft.hsp.server.api.impl;

import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.World;

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
