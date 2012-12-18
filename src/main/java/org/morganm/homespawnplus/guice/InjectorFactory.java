/**
 * 
 */
package org.morganm.homespawnplus.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

/** Class with logic for determining what sort of environment we
 * are running in and returning a dependency injector that will
 * build the appropriate object graph for that environment.
 *  
 * @author morganm
 *
 */
public class InjectorFactory {
    /**
     * Factory to create Guice Injector. Eventually it will look at the environment
     * it is running in and return the appropriate injector to that environment.
     * 
     * @param originalPluginObject the original plugin object, which is passed through
     * to the module specific to the server.
     * 
     * @return
     */
    public static Injector createInjector(Object originalPluginObject) {
        // in the future this will choose different injectors based on the
        // environment. For now the only environment we support is Bukkit.
        Injector injector = Guice.createInjector(new HSPModule(), new BukkitModule(originalPluginObject));
        return injector;
    }
}
