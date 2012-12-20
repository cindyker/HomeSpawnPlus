/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;

import org.morganm.homespawnplus.entity.ObjectFactory;
import org.morganm.homespawnplus.guice.InjectorFactory;
import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.event.EventDispatcher;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.mBukkitLib.JarUtils;
import org.morganm.mBukkitLib.PermissionSystem;
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

    private final Object originalPluginObject;
    private PermissionSystem permSystem;
    private EventDispatcher eventDispatcher;
    private Initializer initializer;
    private Storage storage;
    private Factory factory;
    private JarUtils jarUtil;
    private Plugin plugin;
    
    private String version = "undef";
    private int buildNumber = -1;
    
    public HomeSpawnPlus(Object originalPluginObject) {
        this.originalPluginObject = originalPluginObject;
    }

    public void onEnable() throws Exception {
        final Injector injector = InjectorFactory.createInjector(originalPluginObject);     // IoC container
        injector.injectMembers(this);   // inject all dependencies for this object

        buildNumber = jarUtil.getBuildNumber();
        version = plugin.getVersion();
        
        initializer.initAll();
        ObjectFactory.setFactory(factory);

        permSystem.setupPermissions();
        storage.initializeStorage();
        eventDispatcher.registerEvents();
        
        log.info("version "+version+", build "+buildNumber+" is enabled");
    }
    
    public void onDisable() {
        log.info("version "+version+", build "+buildNumber+" is disabled");
    }

    @Inject
    private void setPermissionSystem(PermissionSystem permSystem) {
        this.permSystem = permSystem;
    }
    
    @Inject
    private void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    @Inject
    private void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }
    
    @Inject
    private void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Inject
    private void setFactory(Factory factory) {
        this.factory = factory;
    }
    
    @Inject
    private void setJarUtils(JarUtils jarUtil) {
        this.jarUtil = jarUtil;
    }

    @Inject
    private void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
}
