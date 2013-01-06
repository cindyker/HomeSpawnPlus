/**
 * 
 */
package org.morganm.homespawnplus.guice;

import org.morganm.homespawnplus.config.ConfigStorage;

import com.google.inject.Guice;
import com.google.inject.Injector;

/** Factory class to create an injector specific to the Bukkit module.
 *  
 * @author morganm
 *
 */
public class BukkitInjectorFactory implements InjectorFactory {
    private final Object originalPluginObject;
    private final ConfigStorage configStorage;
    
    public BukkitInjectorFactory(Object originalPluginObject, ConfigStorage configStorage) {
        this.originalPluginObject = originalPluginObject;
        this.configStorage = configStorage;
    }

    /**
     * Factory to create Guice Injector.
     * 
     * @return
     */
    public Injector createInjector() {
        // in the future this will choose different injectors based on the
        // environment. For now the only environment we support is Bukkit.
        Injector injector = Guice.createInjector(new HSPModule(configStorage), new BukkitModule(originalPluginObject));
        return injector;
    }
}
