package com.andune.minecraft.hsp.integration.dynmap;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.config.ConfigDynmap;
import com.google.inject.Inject;

/**
 * @author andune
 */
public class SpongeDynmapModule implements DynmapModule, Initializable {
    private final Logger log = LoggerFactory.getLogger(SpongeDynmapModule.class);

    private final ConfigDynmap configDynmap;

    @Inject
    public SpongeDynmapModule(ConfigDynmap configDynmap) {
        this.configDynmap = configDynmap;
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
}
