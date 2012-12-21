/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.Plugin;

/** 
 * 
 * @author morganm
 *
 */
@Singleton
public class BukkitPlugin implements Plugin
{
    private final HSPBukkit plugin;
    
    @Inject
    public BukkitPlugin(HSPBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public File getJarFile() {
        return plugin._getJarFile();
    }

    @Override
    public String getName() {
        return plugin.getDescription().getName();
    }

    @Override
    public ClassLoader getClassLoader() {
        return plugin._getClassLoader();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}