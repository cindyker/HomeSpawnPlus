/**
 * 
 */
package org.morganm.homespawnplus.integration;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.util.Debug;

/**
 * @author morganm
 *
 */
public class MultiverseIntegration {
	private final HomeSpawnPlus plugin;
	private MultiverseSafeTeleporter teleporter;
	private MultiverseListener multiverseListener;
	private String currentTeleporter;
	
	public MultiverseIntegration(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	public void onEnable() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if( p != null ) {
			if( p.getDescription().getVersion().startsWith("2.4") ) {
				com.onarandombox.MultiverseCore.MultiverseCore multiverse = (com.onarandombox.MultiverseCore.MultiverseCore) p;
			
				if( multiverse != null ) {
					Debug.getInstance().debug("Hooking Multiverse");
					teleporter = new MultiverseSafeTeleporter(plugin, multiverse);
					teleporter.install();
					multiverseListener = new MultiverseListener(plugin, teleporter);
					registerListener();
				}
			}
		}
	}
	
	public void onDisable() {
		if( teleporter != null ) {
			Debug.getInstance().debug("Unhooking Multiverse");
			teleporter.uninstall();
		}
		teleporter = null;
	}

	private void registerListener() {
        plugin.getServer().getPluginManager().registerEvent(com.onarandombox.MultiverseCore.event.MVTeleportEvent.class,
        		multiverseListener,
        		EventPriority.NORMAL,
        		new EventExecutor() {
        			public void execute(Listener listener, Event event) throws EventException {
        				try {
        					multiverseListener.onMultiverseTeleport((com.onarandombox.MultiverseCore.event.MVTeleportEvent) event);
        				} catch (Throwable t) {
        					throw new EventException(t);
        				}
        			}
		        },
		        plugin);
	}

	public String getCurrentTeleporter() {
		return currentTeleporter;
	}
	
	public void setCurrentTeleporter(String name) {
		this.currentTeleporter = name;
	}
}
