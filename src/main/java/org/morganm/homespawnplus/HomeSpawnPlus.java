/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigStorage;
import org.morganm.homespawnplus.guice.InjectorFactory;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.event.EventDispatcher;
import org.morganm.homespawnplus.util.SpawnUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/** Main object that controls plugin startup and shutdown.
 * 
 * @author morganm
 *
 */
public class HomeSpawnPlus {
    private final Logger log = LoggerFactory.getLogger(HomeSpawnPlus.class);

    // members that are passed in to us when instantiated
    private final Object originalPluginObject;
    private final ConfigStorage configStorage;

    // members that are injected by the IoC container
    @Inject private Initializer initializer;
    @Inject private EventDispatcher eventDispatcher;
    @Inject private Plugin plugin;
    @Inject private SpawnUtil spawnUtil;
    
    public HomeSpawnPlus(Object originalPluginObject, ConfigStorage configStorage) {
        this.originalPluginObject = originalPluginObject;
        this.configStorage = configStorage;
    }

    public void onEnable() throws Exception {
//        GuiceDebug.enable();
        final Injector injector = InjectorFactory.createInjector(originalPluginObject, configStorage); // IoC container
        injector.injectMembers(this);   // inject all dependencies for this object

        initializer.initAll();
        eventDispatcher.registerEvents();
        
        log.info("version {}, build {} is enabled", plugin.getVersion(), plugin.getBuildNumber());
    }
    
    public void onDisable() {
        // unhook multiverse (if needed)
//        multiverse.onDisable();

        spawnUtil.updateAllPlayerLocations();
        initializer.shutdownAll();

        log.info("version {}, build {} is disabled", plugin.getVersion(), plugin.getBuildNumber());
    }
}
