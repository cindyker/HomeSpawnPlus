/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.Economy;
import org.morganm.homespawnplus.server.api.Factory;
import org.morganm.homespawnplus.server.api.Scheduler;
import org.morganm.homespawnplus.server.api.Server;
import org.morganm.homespawnplus.server.api.ServerConfig;
import org.morganm.homespawnplus.server.api.Teleport;
import org.morganm.homespawnplus.server.api.YamlFile;
import org.morganm.homespawnplus.server.api.command.CommandConfig;
import org.morganm.homespawnplus.server.api.event.EventDispatcher;
import org.morganm.homespawnplus.server.bukkit.BukkitEconomy;
import org.morganm.homespawnplus.server.bukkit.BukkitEventDispatcher;
import org.morganm.homespawnplus.server.bukkit.BukkitFactory;
import org.morganm.homespawnplus.server.bukkit.BukkitPlugin;
import org.morganm.homespawnplus.server.bukkit.BukkitScheduler;
import org.morganm.homespawnplus.server.bukkit.BukkitServer;
import org.morganm.homespawnplus.server.bukkit.BukkitServerConfig;
import org.morganm.homespawnplus.server.bukkit.BukkitTeleport;
import org.morganm.homespawnplus.server.bukkit.BukkitYamlConfigFile;
import org.morganm.homespawnplus.server.bukkit.HSPBukkit;
import org.morganm.homespawnplus.server.bukkit.command.BukkitCommandConfig;
import org.morganm.mBukkitLib.JarUtils;
import org.morganm.mBukkitLib.LoggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        bind(org.morganm.homespawnplus.server.api.Plugin.class)
            .to(BukkitPlugin.class);
        bind(Economy.class)
            .to(BukkitEconomy.class);
        
        bind(EbeanServer.class)
            .toInstance(plugin.getDatabase());
        
        bind(YamlFile.class)
            .to(BukkitYamlConfigFile.class);
        
        bind(org.morganm.mBukkitLib.Logger.class)
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
}
