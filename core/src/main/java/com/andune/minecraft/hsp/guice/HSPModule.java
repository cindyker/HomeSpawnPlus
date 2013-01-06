/**
 * 
 */
package com.andune.minecraft.hsp.guice;

import javax.inject.Singleton;

import org.reflections.Reflections;

import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.i18n.LocaleStringReplacerImpl;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.server.api.TeleportOptions;
import com.andune.minecraft.hsp.server.api.event.EventListener;
import com.andune.minecraft.hsp.server.api.impl.TeleportOptionsImpl;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageFactory;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.strategy.StrategyConfig;
import com.andune.minecraft.hsp.strategy.StrategyConfigImpl;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyEngineImpl;
import com.andune.minecraft.hsp.strategy.StrategyResultFactory;
import com.andune.minecraft.hsp.strategy.StrategyResultFactoryImpl;
import com.andune.minecraft.hsp.util.BedUtils;
import com.andune.minecraft.hsp.util.BedUtilsImpl;
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
            .to(com.andune.minecraft.hsp.EventListener.class);
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
