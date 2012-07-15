/**
 * 
 */
package org.morganm.homespawnplus.integration;

import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;

/** Abstraction layer for WorldGuardInterface. This allows this class (and
 * therefore HSP) to load even if WorldGuard isn't installed on the server.
 * 
 * @author morganm
 *
 */
public class WorldGuardIntegration {
	private final HomeSpawnPlus plugin;
	private WorldGuardInterface worldGuardInterface;
	
	public WorldGuardIntegration(HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	public boolean isEnabled() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if( p != null )
			return p.isEnabled();
		else
			return false;
	}

	public WorldGuardInterface getWorldGuardInterface() {
		if( worldGuardInterface == null )
			worldGuardInterface = new WorldGuardInterface(plugin);
		return worldGuardInterface;
	}
}
