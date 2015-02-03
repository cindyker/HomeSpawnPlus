package com.andune.minecraft.hsp.guice;

import com.andune.minecraft.hsp.config.ConfigBootstrap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * @author andune
 */
public class SpongeInjectorFactory implements InjectorFactory {
    private final Game game;
    private final PluginContainer pc;
    private final ConfigBootstrap configBootstrap;

    public SpongeInjectorFactory(Game game, PluginContainer pc, ConfigBootstrap configBootstrap) {
        this.game = game;
        this.pc = pc;
        this.configBootstrap = configBootstrap;
    }

    /**
     * Factory to create Guice Injector.
     *
     * @return
     */
    public Injector createInjector() {
        // in the future this will choose different injectors based on the
        // environment. For now the only environment we support is Bukkit.
        Injector injector = Guice.createInjector(new HSPModule(configBootstrap), new SpongeModule(game, pc));
        return injector;
    }
}
