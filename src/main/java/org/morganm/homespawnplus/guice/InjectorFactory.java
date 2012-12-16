/**
 * 
 */
package org.morganm.homespawnplus.guice;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;

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
    public static Injector createInjector(HomeSpawnPlus plugin) {
        // in the future this will choose different injectors based on the
        // environment. For now the only environment we support is Bukkit.
        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin("HomeSpawnPlus");
        Injector injector = Guice.createInjector(new HSPModule(plugin), new BukkitModule(bukkitPlugin));
        
        return injector;
    }
}
