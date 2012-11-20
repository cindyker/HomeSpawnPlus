/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Singleton;

import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.config.bukkit.BukkitConfig;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** Module which leverages Bukkit's YAML facilities for processing config
 * files.
 * 
 * @author morganm
 *
 */
public class BukkitConfigModule extends AbstractModule {
    private final Plugin plugin;

    public BukkitConfigModule(Plugin plugin) {
        this.plugin = plugin;
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(ConfigCore.class)
            .to(BukkitConfig.class)
            .in(Scopes.SINGLETON);
    }
    
    @Provides
    @Singleton
    protected ConfigCore getConfig() {
        
    }
}
