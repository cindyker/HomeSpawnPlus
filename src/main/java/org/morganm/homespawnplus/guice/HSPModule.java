/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.event.EventListener;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.util.BedUtils;
import org.morganm.homespawnplus.util.BedUtilsImpl;
import org.morganm.mBukkitLib.i18n.Locale;
import org.morganm.mBukkitLib.i18n.LocaleStringReplacerImpl;
import org.reflections.Reflections;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** Guice IoC Injector for HomeSpawnPlus.
 * 
 * @author morganm
 *
 */
public class HSPModule extends AbstractModule {
    private Reflections reflections;
    
    public HSPModule() {
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(EventListener.class)
            .to(org.morganm.homespawnplus.listener.EventListener.class);
        bind(Storage.class)
            .toProvider(StorageProvider.class);
        bind(BedUtils.class)
            .to(BedUtilsImpl.class);
        bind(Locale.class)
            .to(LocaleStringReplacerImpl.class)
            .in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    protected Reflections provideReflections() {
        if( reflections == null )
            reflections = Reflections.collect();
        return reflections;
    }
}
