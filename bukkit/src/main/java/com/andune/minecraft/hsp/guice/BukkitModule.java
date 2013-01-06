/**
 * 
 */
package com.andune.minecraft.hsp.guice;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.LoggerImpl;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.config.ConfigDynmap;
import com.andune.minecraft.hsp.integration.dynmap.DynmapModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseModule;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorderModule;
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
        
        bind(YamlFile.class)
            .to(BukkitYamlConfigFile.class);
        bind(com.andune.minecraft.commonlib.Logger.class)
            .to(LoggerImpl.class);
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
    private MultiverseModule multiverse;
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
    protected WorldBorderModule getWorldBorder() {
        if( worldBorder == null )
            worldBorder = new WorldBorderModule(plugin);
        return worldBorder;
    }
    
    @Provides
    protected MultiverseModule getMultiverse(ConfigCore configCore) {
        if( multiverse == null )
            multiverse = new MultiverseModule(configCore, plugin);
        return multiverse;
    }
    
    @Provides
    protected WorldGuardModule getWorldGuard(BukkitFactory factory, StrategyEngine strategyEngine, Server server) {
        if( worldGuard == null )
            worldGuard = new WorldGuardModule(plugin, factory, strategyEngine, server);
        return worldGuard;
    }
}
