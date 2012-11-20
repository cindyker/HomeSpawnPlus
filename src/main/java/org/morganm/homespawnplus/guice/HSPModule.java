/**
 * 
 */
package org.morganm.homespawnplus.guice;

import java.io.IOException;

import javax.inject.Singleton;

import org.morganm.homespawnplus.HSPNew;
import org.morganm.homespawnplus.Permissions;
import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.config.ConfigEconomy;
import org.morganm.homespawnplus.server.api.events.EventListener;
import org.morganm.homespawnplus.util.HomeUtil;
import org.morganm.mBukkitLib.PermissionSystem;
import org.morganm.mBukkitLib.Teleport;
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
    private HSPNew mainClass;
    private LocaleConfig localeConfig;
    private Locale locale;  // TODO
    
    public HSPModule(HSPNew mainClass) {
        this.mainClass = mainClass;
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(Teleport.class)
            .in(Scopes.SINGLETON);
        bind(PermissionSystem.class)
            .in(Scopes.SINGLETON);
        bind(Permissions.class)
            .in(Scopes.SINGLETON);
        bind(EventListener.class)
            .to(org.morganm.homespawnplus.listener.EventListener.class)
            .in(Scopes.SINGLETON);
        bind(HomeUtil.class)
            .in(Scopes.SINGLETON);
        
        // Configuration objects
        bind(ConfigCore.class)
            .in(Scopes.SINGLETON);
        bind(ConfigEconomy.class)
            .in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    protected HSPNew provideHSPNew() {
        return mainClass;
    }
    
    @Provides
    @Singleton
    protected Locale provideLocale() throws IOException {
        if( locale == null )
            locale = LocaleFactory.getLocale(localeConfig);
        
        return locale;
    }
 }
