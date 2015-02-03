package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.guice.SpongeInjectorFactory;
import com.andune.minecraft.hsp.server.sponge.config.SpongeConfigBootstrap;
import com.andune.minecraft.hsp.util.LogUtil;
import com.google.common.base.Optional;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.config.ConfigRoot;
import org.spongepowered.api.service.config.ConfigService;
import org.spongepowered.api.util.event.Subscribe;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: version should be set by maven
 *
 * @author andune
 */
@Plugin(id = "HomeSpawnPlus", name = "HomeSpawnPlus", version = "2.0-SNAPSHOT")
public class HomeSpawnPlusSponge {
    private HomeSpawnPlus mainClass;
    private PluginContainer pluginContainer;
    private ConfigService configService;

    @Subscribe
    public void initialize(ServerStartedEvent event) {
        LoggerFactory.setLoggerPrefix("[HomeSpawnPlus] ");

        // disable reflections spam; it's a bug that prints warnings that look alarming
        Logger.getLogger("org.reflections").setLevel(Level.OFF);

//        TODO: figure how to to handle enabling debug on Sponge
//        File debugFlagFile = new File(getDataFolder(), "devDebug");
//        if (debugFlagFile.exists())
            LogUtil.enableDebug();

        final Game game = event.getGame();
        final PluginManager pm = game.getPluginManager();
        Optional<PluginContainer> pcRef = pm.getPlugin("HomeSpawnPlus");
        Optional<ConfigService> csRef = game.getServiceManager().provide(ConfigService.class);

        // this will throw an exception if the reference is null, which is fine
        // for now since we'd want this to blow up so we can fix it.
        pluginContainer = pcRef.get();
        configService = csRef.get();

        org.slf4j.Logger log = pm.getLogger(pluginContainer);
        try {
            log.debug("Initializing BukkitInjectorFactory");
            SpongeInjectorFactory factory = new SpongeInjectorFactory(game, pluginContainer,
                    new SpongeConfigBootstrap(getBootstrapConfig()));

            log.debug("Instantiating HomeSpawnPlus mainClass");
            mainClass = new HomeSpawnPlus(factory);

            log.debug("invoking mainClass.onEnable()");
            mainClass.onEnable();
        } catch (Exception e) {
            log.error("Caught exception loading plugin, shutting down", e);
        }

    }

    /**
     * Find and load the bootstrap configuration, this is required prior to
     * handing off control to the core injection routines.
     *
     * @return
     * @throws Exception
     */
    private ConfigRoot getBootstrapConfig() throws Exception {
        // TODO: this probably won't work, Sponge config API is very new and
        // conflicting documentation exists, so this is just here to satisfy
        // the return type dependency for now.
        return configService.getPluginConfig(this);
    }
}
