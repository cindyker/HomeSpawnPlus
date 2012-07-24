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
	private WorldGuardRegion worldGuardRegion;
	
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
	
	public void init() {
		if( !isEnabled() )
			return;
		
		getWorldGuardRegion().registerEvents();
	}

	public WorldGuardInterface getWorldGuardInterface() {
		if( !isEnabled() )
			return null;

		if( worldGuardInterface == null )
			worldGuardInterface = new WorldGuardInterface(plugin);
		return worldGuardInterface;
	}
	
	public WorldGuardRegion getWorldGuardRegion() {
		if( !isEnabled() )
			return null;

		if( worldGuardRegion == null )
			worldGuardRegion = new WorldGuardRegion(plugin);
		return worldGuardRegion;
	}
}
