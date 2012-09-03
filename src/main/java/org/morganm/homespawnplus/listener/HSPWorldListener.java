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
package org.morganm.homespawnplus.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;


/**
 * Handle events for all World related events
 * @author morganm, Timberjaw
 */
public class HSPWorldListener implements Listener {
    private final HomeSpawnPlus plugin;
    private final HomeSpawnUtils util;
    
    public HSPWorldListener(HomeSpawnPlus instance) {
        plugin = instance;
        util = plugin.getUtil();
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event)
    {
    	World w = event.getWorld();
    	
    	//  Set spawn if one has not yet been set (new world)
    	if( util.getSpawn(w.getName()) == null ) {
    		if( util.isVerboseLogging() )
    			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " No global spawn found, setting global spawn to world spawn.");
			util.setSpawn(w.getSpawnLocation(), "onWorldLoad");
		}
    }
}
