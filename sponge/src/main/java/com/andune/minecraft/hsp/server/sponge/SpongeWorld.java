package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andune
 */
public class SpongeWorld implements World {
    final org.spongepowered.api.world.World spongeWorld;

    public SpongeWorld(org.spongepowered.api.world.World world) {
        spongeWorld = world;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.World#getName()
     */
    @Override
    public String getName() {
        if( spongeWorld != null )
            return spongeWorld.getName();
        else
            return null;
    }

    // TODO: figure out how this is done in Sponge
    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        return false;
    }

    // TODO: figure out how this is done in Sponge
    @Override
    public Location getSpawnLocation() {
        return null;
    }

    public org.spongepowered.api.world.World getSpongeWorld() {
        return spongeWorld;
    }

    // TODO: figure out how this is done in Sponge
    @Override
    public int getMaxHeight() {
        return 256;
    }

    // TODO: implement me for Sponge
    @Override
    public List<World> getChildWorlds() {
        /*
        final List<World> children = new ArrayList<World>();
        final String name = getName();
        if( name != null ) {
            org.bukkit.World world = Bukkit.getWorld(name+"_nether");
            if( world != null )
                children.add(new BukkitWorld(world));
            world = Bukkit.getWorld(name+"_the_end");
            if( world != null )
                children.add(new BukkitWorld(world));
        }
        return children;
        */
        return null;
    }

    // TODO: implement me for Sponge
    @Override
    public World getParentWorld() {
        /*
        final String name = getName();
        String baseName = null;
        if( name != null ) {
            if( name.endsWith("_nether") )
                baseName = name.substring(0, name.length()-7);
            else if( name.endsWith("_the_end") )
                baseName = name.substring(0, name.length()-8);
        }

        World world = null;
        if( baseName != null ) {
            org.bukkit.World w = Bukkit.getWorld(baseName);
            if( w != null )
                world = new BukkitWorld(w);
        }
        return world;
        */
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeWorld)) {
            return false;
        }
        if (spongeWorld == null) {
            return false;
        }

        SpongeWorld other = (SpongeWorld) o;
        return spongeWorld.equals(other.spongeWorld);
    }

    public int hashCode() {
        if (spongeWorld != null)
            return spongeWorld.hashCode();
        else
            return super.hashCode();
    }
}
