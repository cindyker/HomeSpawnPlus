package com.andune.minecraft.hsp.integration.essentials;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.google.inject.Inject;

/**
 * @author andune
 */
public class EssentialsModule implements com.andune.minecraft.hsp.integration.Essentials, Initializable {
    private final Logger log = LoggerFactory.getLogger(EssentialsModule.class);

    @Inject
    public EssentialsModule() {
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
