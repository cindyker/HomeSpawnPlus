/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Singleton;

import org.morganm.homespawnplus.config.ConfigStorage;
import org.morganm.homespawnplus.server.api.TeleportOptions;
import org.morganm.homespawnplus.server.api.event.EventListener;
import org.morganm.homespawnplus.server.api.impl.TeleportOptionsImpl;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageFactory;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;
import org.morganm.homespawnplus.strategy.StrategyConfig;
import org.morganm.homespawnplus.strategy.StrategyConfigImpl;
import org.morganm.homespawnplus.strategy.StrategyEngine;
import org.morganm.homespawnplus.strategy.StrategyEngineImpl;
import org.morganm.homespawnplus.strategy.StrategyResultFactory;
import org.morganm.homespawnplus.strategy.StrategyResultFactoryImpl;
import org.morganm.homespawnplus.util.BedUtils;
import org.morganm.homespawnplus.util.BedUtilsImpl;
e.minecraft.commonlib.i18n.LocaleStriimport com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.i18n.LocaleStringReplacerImpl;
ngReplacerImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** Guice IoC Injector for HomeSpawnPlus.
 * 
 * @author morganm
 *
 */
public class HSPModule extends AbstractModule {
    private final ConfigStorage configStorage;
    private Reflections reflections;
    
    public HSPModule(ConfigStorage configStorage) {
        this.configStorage = configStorage;
    }

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(EventListener.class)
            .to(org.morganm.homespawnplus.EventListener.class);
        bind(BedUtils.class)
            .to(BedUtilsImpl.class);
        bind(Locale.class)
            .to(LocaleStringReplacerImpl.class)
            .in(Scopes.SINGLETON);
        bind(StrategyResultFactory.class)
            .to(StrategyResultFactoryImpl.class);
        bind(TeleportOptions.class)
            .to(TeleportOptionsImpl.class);
        bind(StrategyEngine.class)
            .to(StrategyEngineImpl.class);
        bind(StrategyConfig.class)
            .to(StrategyConfigImpl.class);
        
//        bind(Storage.class)
//            .toProvider(StorageProvider.class);
    }
    
    @Provides
    @Singleton
    protected ConfigStorage getConfigStorage() {
        return configStorage;
    }

    @Provides
    @Singleton
    protected Storage getStorage(StorageFactory storageFactory) {
        return storageFactory.getInstance();
    }

    @Provides
    @Singleton
    protected Reflections provideReflections() {
        if( reflections == null )
            reflections = Reflections.collect();
        return reflections;
    }
    
    @Provides
    @Singleton
    protected SpawnDAO provideSpawnDAO(Storage storage) {
        return storage.getSpawnDAO();
    }

    @Provides
    @Singleton
    protected PlayerDAO providePlayerDAO(Storage storage) {
        return storage.getPlayerDAO();
    }

    @Provides
    @Singleton
    protected PlayerSpawnDAO providePlayerSpawnDAO(Storage storage) {
        return storage.getPlayerSpawnDAO();
    }

    @Provides
    @Singleton
    protected HomeDAO provideHomeDAO(Storage storage) {
        return storage.getHomeDAO();
    }
}