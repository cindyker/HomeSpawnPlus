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
import org.morganm.homespawnplus.config.ConfigOptions;
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
	private String sourcePortalName;
	private String destinationPortalName;
	
	public MultiverseIntegration(final HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	public boolean isEnabled() {
		return plugin.getConfig().getBoolean(ConfigOptions.MULTIVERSE_INTEGRATION_ENABLED, false);
	}
	
	public boolean isMultiverseEnabled() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		return p.isEnabled();
	}
	public boolean isMultiversePortalsEnabled() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
		return p.isEnabled();
	}
	
	public void onEnable() {
		if( !isEnabled() )
			return;
		
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if( p != null ) {
			if( p.getDescription().getVersion().startsWith("2.4") ) {
				com.onarandombox.MultiverseCore.MultiverseCore multiverse = (com.onarandombox.MultiverseCore.MultiverseCore) p;
			
				if( multiverse != null ) {
					Debug.getInstance().debug("Hooking Multiverse");
					teleporter = new MultiverseSafeTeleporter(plugin, multiverse);
					teleporter.install();
					multiverseListener = new MultiverseListener(plugin, teleporter);
					registerListeners();
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

	public String getCurrentTeleporter() {
		return currentTeleporter;
	}
	
	public void setCurrentTeleporter(String name) {
		this.currentTeleporter = name;
	}
	
	public String getSourcePortalName() {
		return sourcePortalName;
	}
	
	public void setSourcePortalName(String sourcePortalName) {
		this.sourcePortalName = sourcePortalName;
	}
	
	public String getDestinationPortalName() {
		return destinationPortalName;
	}
	
	public void setDestinationPortalName(String destinationPortalName) {
		this.destinationPortalName = destinationPortalName;
	}
	
	private void registerListeners() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if( p != null ) {
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
        
		p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
		if( p != null ) {
	        plugin.getServer().getPluginManager().registerEvent(com.onarandombox.MultiversePortals.event.MVPortalEvent.class,
	        		multiverseListener,
	        		EventPriority.NORMAL,
	        		new EventExecutor() {
	        			public void execute(Listener listener, Event event) throws EventException {
	        				try {
	        					multiverseListener.onMultiversePortalEvent((com.onarandombox.MultiversePortals.event.MVPortalEvent) event);
	        				} catch (Throwable t) {
	        					throw new EventException(t);
	        				}
	        			}
			        },
			        plugin);
		}
	}
}
