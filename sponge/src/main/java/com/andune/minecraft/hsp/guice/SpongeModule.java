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
package com.andune.minecraft.hsp.guice;

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.server.api.Economy;
import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.commonlib.server.api.Scheduler;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.commonlib.server.api.event.EventDispatcher;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.config.ConfigDynmap;
import com.andune.minecraft.hsp.integration.Essentials;
import com.andune.minecraft.hsp.integration.dynmap.DynmapModule;
import com.andune.minecraft.hsp.integration.dynmap.SpongeDynmapModule;
import com.andune.minecraft.hsp.integration.essentials.EssentialsModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCore;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCoreModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortals;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortalsModule;
import com.andune.minecraft.hsp.integration.vault.Vault;
import com.andune.minecraft.hsp.integration.vault.VaultModule;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorderModule;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuardModule;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.server.core.EconomyImpl;
import com.andune.minecraft.hsp.server.sponge.*;
import com.andune.minecraft.hsp.storage.EbeanServerFactory;
import com.andune.minecraft.hsp.storage.SpongeStorageFactory;
import com.andune.minecraft.hsp.storage.StorageFactory;
import com.andune.minecraft.hsp.storage.ebean.EBeanUtils;
import com.andune.minecraft.hsp.storage.ebean.SpongeEBeanUtils;
import com.avaje.ebean.EbeanServer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;

/**
 * @author andune
 */
public class SpongeModule extends AbstractModule {
    private final Game game;
    private final PluginContainer pluginContainer;
    private final File pluginDataFolder;

    public SpongeModule(Game game, PluginContainer pluginContainer, File pluginDataFolder) {
        this.game = game;
        this.pluginContainer = pluginContainer;
        this.pluginDataFolder = pluginDataFolder;
    }

    @Override
    protected void configure() {
        bind(Server.class)
                .to(SpongeServer.class);
        bind(com.andune.minecraft.commonlib.server.api.Server.class)
                .to(SpongeServer.class);

        bind(Factory.class)
                .to(SpongeFactory.class);
        bind(SpongeFactoryInterface.class)
                .to(SpongeFactory.class);
        bind(com.andune.minecraft.commonlib.server.api.Factory.class)
                .to(SpongeFactory.class);

        bind(Economy.class)
                .to(EconomyImpl.class);
        bind(com.andune.minecraft.commonlib.server.api.Plugin.class)
                .to(SpongePlugin.class);
        bind(PermissionSystem.class)
                .to(SpongePermissionSystem.class);
        bind(Scheduler.class)
                .to(SpongeScheduler.class);
        bind(StorageFactory.class)
                .to(SpongeStorageFactory.class);
        bind(Teleport.class)
                .to(SpongeTeleport.class);
        bind(EventDispatcher.class)
                .to(SpongeEventDispatcher.class);
        bind(EBeanUtils.class)
                .to(SpongeEBeanUtils.class);
    }

    private SpongePlugin spongePlugin;
    @Provides
    @Singleton
    protected SpongePlugin getPlugin(PluginContainer pluginContainer, JarUtils jarUtil) {
        if (spongePlugin == null) {
            spongePlugin = new SpongePlugin(pluginContainer, jarUtil, pluginDataFolder);
        }

        return spongePlugin;
    }

    private EbeanServer ebeanServer;
    @Provides
    @Singleton
    protected EbeanServer getEbeanServer(EbeanServerFactory factory) {
        if (ebeanServer == null) {
            ebeanServer = factory.getEbeanServer(pluginContainer.getName());
        }

        return ebeanServer;
    }

    @Provides
    @Singleton
    protected PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    @Provides
    @Singleton
    protected Game provideGame() {
        return game;
    }

    @Provides
    @Singleton
    protected JarUtils getJarUtils() {
        // TODO: find way to pass in JAR file or another way to implement this
        return new JarUtils(pluginDataFolder, null);
    }

    ///////////////
    // Integration objects below this point, they must supply their own
    // instances, because if we let Guice try to auto-wire them, it will walk
    // the class members and blow up when it can't process member variables
    // that don't exist, as is often the case for optional plugin dependencies.
    //
    // Also note, the implementation object itself must be returned for the
    // interface, otherwise the Initializer will initialize a different object
    // than the one the interface returns.
    ///////////////

    private MultiverseCoreModule multiverseCore;
    private MultiversePortalsModule multiversePortals;
    private WorldGuardModule worldGuard;
    private WorldBorderModule worldBorder;
    private VaultModule vault;
    private SpongeDynmapModule dynmap;
    private EssentialsModule essentials;

    @Provides
    protected MultiverseCoreModule getMultiverseCoreModule(ConfigCore configCore) {
        if (multiverseCore == null) {
            multiverseCore = new MultiverseCoreModule(configCore);
        }
        return multiverseCore;
    }
    @Provides
    protected MultiverseCore getMultiverseCore(ConfigCore configCore) {
        return getMultiverseCoreModule(configCore);
    }

    @Provides
    protected MultiversePortalsModule getMultiversePortalsModule(ConfigCore configCore, MultiverseCoreModule mvCore) {
        if (multiversePortals == null) {
            multiversePortals = new MultiversePortalsModule(configCore, mvCore);
        }
        return multiversePortals;
    }
    @Provides
    protected MultiversePortals getMultiversePortals(ConfigCore configCore, MultiverseCoreModule mvCore) {
        return getMultiversePortalsModule(configCore, mvCore);
    }

    @Provides
    protected WorldGuardModule getWorldGuardModule() {
        if (worldGuard == null) {
            worldGuard = new WorldGuardModule();
        }

        return worldGuard;
    }
    @Provides
    protected WorldGuard getWorldGuard() {
        return getWorldGuardModule();
    }

    @Provides
    protected WorldBorderModule getWorldBorderModule() {
        if (worldBorder == null)
            worldBorder = new WorldBorderModule();
        return worldBorder;
    }
    @Provides
    protected WorldBorder getWorldBorder() {
        return getWorldBorderModule();
    }

    @Provides
    protected VaultModule getVaultModule() {
        if (vault == null)
            vault = new VaultModule();
        return vault;
    }
    @Provides
    protected Vault getVault() {
        return getVaultModule();
    }

    @Provides
    protected SpongeDynmapModule getDynmapModule(ConfigDynmap configDynmap) {
        if (dynmap == null)
            dynmap = new SpongeDynmapModule(configDynmap);
        return dynmap;
    }
    @Provides
    protected DynmapModule getDynmapModule(SpongeDynmapModule bukkitDynmapModule) {
        return bukkitDynmapModule;
    }

    @Provides
    protected EssentialsModule getEssentialsModule() {
        if (essentials == null)
            essentials = new EssentialsModule();
        return essentials;
    }
    @Provides
    protected Essentials getEssentials() {
        return getEssentialsModule();
    }
}
