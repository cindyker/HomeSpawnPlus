/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.config.ConfigDynmap;
import com.andune.minecraft.hsp.integration.dynmap.DynmapModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCore;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCoreModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseListener;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortals;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortalsModule;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorderModule;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuardModule;
import com.andune.minecraft.hsp.server.api.Economy;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.PermissionSystem;
import com.andune.minecraft.hsp.server.api.Scheduler;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.server.api.ServerConfig;
import com.andune.minecraft.hsp.server.api.Teleport;
import com.andune.minecraft.hsp.server.api.YamlFile;
import com.andune.minecraft.hsp.server.api.command.CommandConfig;
import com.andune.minecraft.hsp.server.api.event.EventDispatcher;
import com.andune.minecraft.hsp.server.bukkit.BukkitEconomy;
import com.andune.minecraft.hsp.server.bukkit.BukkitEventDispatcher;
import com.andune.minecraft.hsp.server.bukkit.BukkitFactory;
import com.andune.minecraft.hsp.server.bukkit.BukkitPermissionSystem;
import com.andune.minecraft.hsp.server.bukkit.BukkitPlugin;
import com.andune.minecraft.hsp.server.bukkit.BukkitScheduler;
import com.andune.minecraft.hsp.server.bukkit.BukkitServer;
import com.andune.minecraft.hsp.server.bukkit.BukkitServerConfig;
import com.andune.minecraft.hsp.server.bukkit.BukkitTeleport;
import com.andune.minecraft.hsp.server.bukkit.BukkitYamlConfigFile;
import com.andune.minecraft.hsp.server.bukkit.HSPBukkit;
import com.andune.minecraft.hsp.server.bukkit.command.BukkitCommandConfig;
import com.andune.minecraft.hsp.storage.BukkitStorageFactory;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageFactory;
import com.andune.minecraft.hsp.storage.ebean.BukkitEBeanUtils;
import com.andune.minecraft.hsp.storage.ebean.EBeanUtils;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.avaje.ebean.EbeanServer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/** This module defines the interface bindings that are specific to Bukkit.
 * 
 * @author morganm
 *
 */
public class BukkitModule extends AbstractModule {
    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(BukkitModule.class);
    
    private final HSPBukkit plugin;

    public BukkitModule(Object originalPluginObject) {
        this.plugin = (HSPBukkit) originalPluginObject;
    }
    
    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void configure() {
        bind(Server.class)
            .to(BukkitServer.class);
        bind(EventDispatcher.class)
            .to(BukkitEventDispatcher.class);
        
        bind(Factory.class)
            .to(BukkitFactory.class);
        bind(org.bukkit.Server.class)
            .toInstance(plugin.getServer());
        bind(Scheduler.class)
            .to(BukkitScheduler.class);
        bind(ServerConfig.class)
            .to(BukkitServerConfig.class);
        bind(Teleport.class)
            .to(BukkitTeleport.class);
        bind(com.andune.minecraft.hsp.server.api.Plugin.class)
            .to(BukkitPlugin.class);
        bind(PermissionSystem.class)
            .to(BukkitPermissionSystem.class);
        
        bind(EbeanServer.class)
            .toInstance(plugin.getDatabase());
        bind(StorageFactory.class)
            .to(BukkitStorageFactory.class);
        bind(EBeanUtils.class)
            .to(BukkitEBeanUtils.class);
        
        bind(YamlFile.class)
            .to(BukkitYamlConfigFile.class);
        bind(com.andune.minecraft.commonlib.Logger.class)
            .to(com.andune.minecraft.commonlib.LoggerImpl.class);
    }

    @Provides
    protected CommandConfig getCommandConfig() {
        BukkitCommandConfig config = new BukkitCommandConfig();
        config.setConfigSection(plugin.getConfig().getConfigurationSection("commands"));
        return config;
    }
    
    @Provides
    @Singleton
    protected HSPBukkit getHSPBukkit() {
        return plugin;
    }
    
    @Provides
    @Singleton
    protected org.bukkit.plugin.Plugin getPlugin() {
        return plugin;
    }
    
    @Provides
    @Singleton
    protected JarUtils getJarUtils() {
        return new JarUtils(plugin.getDataFolder(), plugin._getJarFile());
    }

    ///////////////
    // Integration objects below this point, they must supply their own
    // instances, because if we let Guice try to auto-wire them, it will walk
    // the class members and blow up when it can't process member variables
    // that don't exist, as is often the case for optional plugin dependencies.
    ///////////////
    private BukkitEconomy economy;
    private DynmapModule dynmap;
    private WorldBorderModule worldBorder;
    private MultiverseCoreModule multiverseCore;
    private MultiversePortalsModule multiversePortals;
    private MultiverseListener multiverseListener;
    private WorldGuardModule worldGuard;
    
    @Provides
    protected Economy getEconomy() {
        return getBukkitEconomy();
    }

    @Provides
    protected com.andune.minecraft.hsp.server.bukkit.BukkitEconomy getBukkitEconomy() {
        if( economy == null )
            economy = new BukkitEconomy();
        return economy;
    }

    @Provides
    protected DynmapModule getDynmapModule(ConfigDynmap configDynmap, Storage storage) {
        if( dynmap == null )
            dynmap = new DynmapModule(plugin, configDynmap, storage);
        return dynmap;
    }
    
    @Provides
    protected WorldBorder getWorldBorder() {
        if( worldBorder == null )
            worldBorder = new WorldBorderModule(plugin);
        return worldBorder;
    }
    
    @Provides
    protected MultiverseCore getMultiverseCore(ConfigCore configCore) {
        if( multiverseCore == null ) {
            if( multiverseListener == null )
                multiverseListener = new MultiverseListener();
            multiverseCore = new MultiverseCoreModule(configCore, plugin, multiverseListener);
        }
        return multiverseCore;
    }
    
    @Provides
    protected MultiversePortals getMultiversePortals(ConfigCore configCore) {
        if( multiversePortals == null ) {
            if( multiverseListener == null )
                multiverseListener = new MultiverseListener();
            multiversePortals = new MultiversePortalsModule(configCore, plugin, multiverseListener);
        }
        return multiversePortals;
    }

    @Provides
    protected WorldGuard getWorldGuard(BukkitFactory factory, StrategyEngine strategyEngine, Server server) {
        if( worldGuard == null )
            worldGuard = new WorldGuardModule(plugin, factory, strategyEngine, server);
        return worldGuard;
    }
}
