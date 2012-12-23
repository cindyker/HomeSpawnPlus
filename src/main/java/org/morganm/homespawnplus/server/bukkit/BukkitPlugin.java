/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.mBukkitLib.JarUtils;

/** 
 * 
 * @author morganm
 *
 */
@Singleton
public class BukkitPlugin implements Plugin
{
    private final HSPBukkit plugin;
    private final JarUtils jarUtil;
    private int buildNumber = -1;
    
    @Inject
    public BukkitPlugin(HSPBukkit plugin, JarUtils jarUtil) {
        this.plugin = plugin;
        this.jarUtil = jarUtil;
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

    @Override
    public int getBuildNumber() {
        if( buildNumber == -1 )
            buildNumber = jarUtil.getBuildNumber();
        return buildNumber;
    }
}
