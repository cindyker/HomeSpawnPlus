package com.andune.minecraft.hsp.integration.worldborder;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.google.inject.Inject;

/**
 * TODO: needs implementation for Sponge
 * f
 * @author andune
 */
public class WorldBorderModule implements WorldBorder, Initializable {
    private final Logger log = LoggerFactory.getLogger(WorldBorderModule.class);

    @Inject
    public WorldBorderModule() {
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
    public BorderData getBorderData(String worldName) {
        return null;
    }
}
