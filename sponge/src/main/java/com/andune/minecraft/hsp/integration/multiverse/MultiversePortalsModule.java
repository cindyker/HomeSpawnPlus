package com.andune.minecraft.hsp.integration.multiverse;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.google.inject.Inject;

/**
 * TODO: implement some day when Multiverse is supported on Sponge
 *
 * @author andune
 */
public class MultiversePortalsModule implements MultiversePortals, Initializable {
    private static final Logger log = LoggerFactory.getLogger(MultiversePortalsModule.class);

    private final ConfigCore configCore;
    private final MultiverseCoreModule mvCore;
    private String sourcePortalName;
    private String destinationPortalName;

    @Inject
    public MultiversePortalsModule(ConfigCore configCore, MultiverseCoreModule mvCore) {
        this.configCore = configCore;
        this.mvCore = mvCore;
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
    public String getSourcePortalName() {
        return sourcePortalName;
    }

    @Override
    public void setSourcePortalName(String sourcePortalName) {
        this.sourcePortalName = sourcePortalName;
    }

    @Override
    public String getDestinationPortalName() {
        return destinationPortalName;
    }

    @Override
    public void setDestinationPortalName(String destinationPortalName) {
        this.destinationPortalName = destinationPortalName;
    }
}
