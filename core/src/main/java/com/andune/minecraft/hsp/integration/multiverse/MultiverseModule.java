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
package com.andune.minecraft.hsp.integration.multiverse;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.config.ConfigCore;

/**
 * @author morganm
 *
 */
@Singleton
public class MultiverseModule implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MultiverseModule.class);
    
	private final Plugin plugin;
    private final ConfigCore configCore;
	private MultiverseSafeTeleporter teleporter;
	private MultiverseListener multiverseListener;
	private String currentTeleporter;
	private String sourcePortalName;
	private String destinationPortalName;
	
	@Inject
	public MultiverseModule(ConfigCore configCore, Plugin plugin) {
	    this.configCore = configCore;
		this.plugin = plugin;
	}
	
    @Override
    public int getInitPriority() {
        return 9;
    }

	public boolean isEnabled() {
		return configCore.isMultiverseEnabled();
	}
	
	public String getCoreVersion() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if( p != null )
            return p.getDescription().getVersion();
        else
            return null;
	}

    public String getPortalsVersion() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");
        if( p != null )
            return p.getDescription().getVersion();
        else
            return "null";
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
	
    @Override
	public void init() {
		if( !isEnabled() )
			return;
		
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if( p != null ) {
			if( p.getDescription().getVersion().startsWith("2.4") ) {
				com.onarandombox.MultiverseCore.MultiverseCore multiverse = (com.onarandombox.MultiverseCore.MultiverseCore) p;
			
				if( multiverse != null ) {
					log.debug("Hooking Multiverse");
					teleporter = new MultiverseSafeTeleporter(multiverse, this);
					teleporter.install();
					multiverseListener = new MultiverseListener(this);
					registerListeners();
				}
			}
		}
	}
	
    @Override
	public void shutdown() {
		if( teleporter != null ) {
			log.debug("Unhooking Multiverse");
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
