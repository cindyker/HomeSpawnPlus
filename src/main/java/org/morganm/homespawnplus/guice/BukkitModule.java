/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Singleton;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.morganm.homespawnplus.server.api.Location;
import org.morganm.homespawnplus.server.api.Scheduler;
import org.morganm.homespawnplus.server.api.Server;
import org.morganm.homespawnplus.server.api.ServerConfig;
import org.morganm.homespawnplus.server.api.YamlFile;
import org.morganm.homespawnplus.server.api.command.CommandConfig;
import org.morganm.homespawnplus.server.api.command.CommandRegister;
import org.morganm.homespawnplus.server.api.events.EventDispatcher;
import org.morganm.homespawnplus.server.bukkit.BukkitLocation;
import org.morganm.homespawnplus.server.bukkit.BukkitScheduler;
import org.morganm.homespawnplus.server.bukkit.BukkitServer;
import org.morganm.homespawnplus.server.bukkit.BukkitServerConfig;
import org.morganm.homespawnplus.server.bukkit.BukkitYamlConfigFile;
import org.morganm.homespawnplus.server.bukkit.command.BukkitCommandConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** This module defines the interface bindings that are specific to Bukkit.
 * 
 * @author morganm
 *
 */
public class BukkitModule extends AbstractModule {
    private final Logger log = LoggerFactory.getLogger(BukkitModule.class);
    
    private final Plugin plugin;
    private Economy vaultEconomy;

    public BukkitModule(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(Location.class)
            .to(BukkitLocation.class);
        bind(Server.class)
            .to(BukkitServer.class);
        bind(EventDispatcher.class)
            .to(org.morganm.homespawnplus.server.bukkit.EventDispatcher.class);
        
        bind(CommandRegister.class)
            .to(org.morganm.homespawnplus.server.bukkit.command.BukkitCommandRegister.class);
//        bind(CommandConfig.class)
//            .to(org.morganm.homespawnplus.server.bukkit.command.BukkitCommandConfig.class);
        
        bind(org.bukkit.Server.class)
            .toInstance(plugin.getServer());
        bind(Scheduler.class)
            .to(BukkitScheduler.class)
            .in(Scopes.SINGLETON);
        bind(ServerConfig.class)
            .to(BukkitServerConfig.class).
            in(Scopes.SINGLETON);
        
        bind(YamlFile.class)
            .to(BukkitYamlConfigFile.class);
        bind(YamlConfiguration.class);
    }

    @Provides
    protected CommandConfig getCommandConfig() {
        BukkitCommandConfig config = new BukkitCommandConfig();
        config.setConfigSection(plugin.getConfig().getConfigurationSection("commands"));
        return config;
    }
    
    @Provides
    @Singleton
    protected Plugin getPlugin() {
        return plugin;
    }
    
    @Provides
    protected Economy getEconomy() {
        if( vaultEconomy == null ) {
            Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
            if( vault != null ) {
                RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null) {
                    vaultEconomy = economyProvider.getProvider();
                    log.info("Vault interface found and will be used for economy-related functions");
                }
            }
            else
                log.info("Vault not found, HSP economy features are disabled");
        }
        
        return vaultEconomy;
    }
}
