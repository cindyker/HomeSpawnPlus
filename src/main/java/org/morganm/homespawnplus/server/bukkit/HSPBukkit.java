/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;

/** This class is the interface to Bukkit's Plugin interface. This is abstracted from
 * the rest of the plugin so as to minimize impact to the code when Bukkit makes
 * API changes and to simplify supporting MC-API, Spout or other frameworks.
 * 
 * @author morganm
 *
 */
public class HSPBukkit extends JavaPlugin {
    private HomeSpawnPlus mainClass;

    @Override
    public void onEnable() {
        mainClass = new HomeSpawnPlus(this);
        try {
            mainClass.onEnable();
        }
        catch(Exception e) {
            getLogger().log(Level.SEVERE, "Caught exception loading plugin, shutting down", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if( mainClass != null )
            mainClass.onDisable();
    }
    
    @Override
    public List<Class<?>> getDatabaseClasses() {
        return StorageEBeans.getDatabaseClasses();
    }
    
    public File _getJarFile() {
        System.out.println("_getJarFile()="+super.getFile());
        return super.getFile();
    }

    public ClassLoader _getClassLoader() {
        return super.getClassLoader();
    }
}
