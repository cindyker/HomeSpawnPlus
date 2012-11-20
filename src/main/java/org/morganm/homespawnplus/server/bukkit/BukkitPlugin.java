/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.HSPNew;
import org.morganm.homespawnplus.server.api.Plugin;

/** This class is the interface to Bukkit's Plugin interface. This is abstracted from
 * the rest of the plugin so as to minimize impact to the code when Bukkit makes
 * API changes and to simplify migrating to MC-API someday.
 * 
 * @author morganm
 *
 */
public class BukkitPlugin extends JavaPlugin implements Plugin
{
    private HSPNew mainClass;
    
    @Override
    public void onEnable() {
        mainClass = new HSPNew();
        mainClass.onEnable();
    }
    
    @Override
    public void onDisable() {
        if( mainClass != null )
            mainClass.onDisable();
    }
}
