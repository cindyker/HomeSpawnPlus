/**
 * 
 */
package org.morganm.homespawnplus.guice;

import java.io.IOException;

import javax.inject.Singleton;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.event.EventListener;
import org.morganm.mBukkitLib.i18n.Locale;
import org.morganm.mBukkitLib.i18n.LocaleConfig;
import org.morganm.mBukkitLib.i18n.LocaleFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** Guice IoC Injector for HomeSpawnPlus.
 * 
 * @author morganm
 *
 */
public class HSPModule extends AbstractModule {
    private HomeSpawnPlus mainClass;
    private LocaleConfig localeConfig;
    private Locale locale;
    
    public HSPModule(HomeSpawnPlus mainClass) {
        this.mainClass = mainClass;
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(EventListener.class)
            .to(org.morganm.homespawnplus.listener.EventListener.class)
            .in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    protected HomeSpawnPlus provideHSPNew() {
        return mainClass;
    }
    
    @Provides
    @Singleton
    protected LocaleConfig provideLocaleConfig(Plugin plugin, ConfigCore config) {
        if( localeConfig == null )
            localeConfig = new LocaleConfig(config.getLocale(),
                    plugin.getDataFolder(), "homespawnplus", plugin.getJarFile(), null);
        
        return localeConfig;
    }
    
    /**
     * Guice documentation recommends that providers should not throw exceptions.
     * We do in this case because if getLocale() throws an exception, injection
     * should stop since the plugin cannot run without it.
     * 
     * @param localeConfig
     * @return
     * @throws IOException
     */
    @Provides
    @Singleton
    protected Locale provideLocale(LocaleConfig localeConfig) throws IOException {
        if( locale == null )
                locale = LocaleFactory.getLocale(localeConfig);

        return locale;
    }
 }
