package com.andune.minecraft.hsp.integration.multiverse;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.google.inject.Inject;

/**
 * TODO: implement some day when Multiverse is supported on Sponge
 *
 * @author andune
 */
public class MultiverseCoreModule implements MultiverseCore, Initializable {
    private static final Logger log = LoggerFactory.getLogger(MultiverseCoreModule.class);

    private final ConfigCore configCore;
    private String currentTeleporter;

    @Inject
    public MultiverseCoreModule(ConfigCore configCore) {
        this.configCore = configCore;
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
    public String getCurrentTeleporter() {
        return currentTeleporter;
    }

    @Override
    public void setCurrentTeleporter(String name) {
        currentTeleporter = name;
    }
}
