/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.mBukkitLib.i18n.LocaleConfig;

import com.google.inject.Provider;

/**
 * @author morganm
 * @deprecated
 *
 */
public class LocaleConfigProvider implements Provider<LocaleConfig> {
    private final Plugin plugin;
    private final ConfigCore config;
    
    @Inject
    LocaleConfigProvider(Plugin plugin, ConfigCore config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public LocaleConfig get() {
        return new LocaleConfig(config.getLocale(),
                plugin.getDataFolder(), "homespawnplus", plugin.getJarFile(), null);
    }

}
