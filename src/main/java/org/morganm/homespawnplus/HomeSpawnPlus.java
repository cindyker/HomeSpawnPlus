/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;

import org.morganm.homespawnplus.guice.InjectorFactory;
import org.morganm.homespawnplus.server.api.event.EventDispatcher;
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
    
    private String version = "undef";
    private String buildNumber = "-1";
    
    public HomeSpawnPlus(Object originalPluginObject) {
        this.originalPluginObject = originalPluginObject;
    }

    public void onEnable() throws Exception {
        final Injector injector = InjectorFactory.createInjector(originalPluginObject);     // IoC container
        injector.injectMembers(this);   // inject all dependencies for this object

        initializer.initAll();
        
        permSystem.setupPermissions();
        eventDispatcher.registerEvents();
        
        log.info("version "+version+", build "+buildNumber+" is enabled");
    }
    
    public void onDisable() {
        log.info("version "+version+", build "+buildNumber+" is disabled");
    }

    @Inject
    public void setPermissionSystem(PermissionSystem permSystem) {
        this.permSystem = permSystem;
    }
    
    @Inject
    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    @Inject
    public void setInitializer(Initializer initializer) {
        this.initializer = initializer;
    }
}
