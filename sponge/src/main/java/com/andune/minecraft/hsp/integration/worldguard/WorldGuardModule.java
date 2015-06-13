package com.andune.minecraft.hsp.integration.worldguard;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.google.inject.Inject;

/**
 * TODO: needs implementation for Sponge
 *
 * @author andune
 */
public class WorldGuardModule implements WorldGuard, Initializable {
    private final Logger log = LoggerFactory.getLogger(WorldGuardModule.class);

    @Inject
    public WorldGuardModule() {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

    @Override
    public ProtectedRegion getProtectedRegion(World world, String regionName) {
        return null;
    }

    @Override
    public Location getWorldGuardSpawnLocation(Location location) {
        return null;
    }

    @Override
    public void registerRegion(World world, String regionName) {
    }

    @Override
    public boolean isLocationInRegion(Location l, String regionName) {
        return false;
    }
}