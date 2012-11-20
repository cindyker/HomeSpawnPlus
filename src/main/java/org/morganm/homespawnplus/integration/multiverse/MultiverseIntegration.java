/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.integration.multiverse;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.config.old.ConfigOptions;
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
		if( p != null )
			return p.isEnabled();
		else
			return false;
	}
	public boolean isMultiversePortalsEnabled() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
		if( p != null )
			return p.isEnabled();
		else
			return false;
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
