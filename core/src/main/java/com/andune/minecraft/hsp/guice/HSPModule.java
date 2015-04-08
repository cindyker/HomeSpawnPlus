/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp.guice;

import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.i18n.Locale;
import com.andune.minecraft.commonlib.i18n.LocaleStringReplacerImpl;
import com.andune.minecraft.commonlib.server.api.TeleportOptions;
import com.andune.minecraft.commonlib.server.api.event.EventListener;
import com.andune.minecraft.commonlib.server.api.impl.TeleportOptionsImpl;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.PermissionsImpl;
import com.andune.minecraft.hsp.config.ConfigBootstrap;
import com.andune.minecraft.hsp.config.ConfigLoader;
import com.andune.minecraft.hsp.config.ConfigLoaderImpl;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageFactory;
import com.andune.minecraft.hsp.storage.dao.*;
import com.andune.minecraft.hsp.strategy.*;
import com.andune.minecraft.hsp.util.BedUtils;
import com.andune.minecraft.hsp.util.BedUtilsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import org.reflections.Reflections;

import javax.inject.Singleton;

/**
 * Guice IoC Injector for HomeSpawnPlus.
 *
 * @author andune
 */
public class HSPModule extends AbstractModule {
    private final ConfigBootstrap configBootstrap;
    private Reflections reflections;

    public HSPModule(ConfigBootstrap configBootstrap) {
        this.configBootstrap = configBootstrap;
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
        bind(Colors.class)
                .in(Scopes.SINGLETON);
        bind(TeleportOptions.class)
                .to(TeleportOptionsImpl.class);
        bind(ConfigLoader.class)
                .to(ConfigLoaderImpl.class)
                .in(Scopes.SINGLETON);
        bind(Permissions.class)
                .to(PermissionsImpl.class)
                .in(Scopes.SINGLETON);

        bind(StrategyResultFactory.class)
                .to(StrategyResultFactoryImpl.class);
        bind(StrategyEngine.class)
                .to(StrategyEngineImpl.class);
        bind(StrategyConfig.class)
                .to(StrategyConfigImpl.class);
        bind(StrategyContext.class)
                .to(StrategyContextImpl.class);
    }

    @Provides
    @Singleton
    protected ConfigBootstrap getConfigBootstrap() {
        return configBootstrap;
    }

    @Provides
    @Singleton
    protected ConfigStorage getConfigStorage() {
        return configBootstrap;
    }

    @Provides
    @Singleton
    protected Storage getStorage(StorageFactory storageFactory) {
        return storageFactory.getInstance();
    }

    @Provides
    @Singleton
    protected Reflections provideReflections() {
        if (reflections == null) {
            this.reflections = new Reflections("com.andune.minecraft.hsp");
        }
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

    @Provides
    @Singleton
    protected PlayerLastLocationDAO providePlayerLastLocationDAO(Storage storage) {
        return storage.getPlayerLastLocationDAO();
    }
}
