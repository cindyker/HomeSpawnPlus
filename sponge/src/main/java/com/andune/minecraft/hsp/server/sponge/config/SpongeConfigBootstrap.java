package com.andune.minecraft.hsp.server.sponge.config;

import com.andune.minecraft.hsp.config.ConfigBootstrap;
import com.andune.minecraft.hsp.config.ConfigStorage;
import org.spongepowered.api.service.config.ConfigRoot;

/**
 * @author andune
 */
public class SpongeConfigBootstrap implements ConfigBootstrap {
    public SpongeConfigBootstrap(ConfigRoot bootstrapConfig) {
        // TODO: do something useful
    }

    @Override
    public Type getStorageType() {
        return ConfigStorage.Type.getType("ebeans");
    }

    @Override
    public boolean useInMemoryCache() {
        return false;
    }

    @Override
    public boolean isWarnMissingConfigItems() {
        return true;
    }
}
