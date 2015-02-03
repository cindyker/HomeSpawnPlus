package com.andune.minecraft.hsp.guice;

import com.google.inject.AbstractModule;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * @author andune
 */
public class SpongeModule extends AbstractModule {
    private final Game game;
    private final PluginContainer pc;

    public SpongeModule(Game game, PluginContainer pc) {
        this.game = game;
        this.pc = pc;
    }

    @Override
    protected void configure() {
        // TODO: build stuff and add it here
    }
}
