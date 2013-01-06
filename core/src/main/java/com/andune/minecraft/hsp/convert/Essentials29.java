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
package com.andune.minecraft.hsp.convert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.andune.minecraft.hsp.entity.HomeImpl;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.OfflinePlayer;
import com.andune.minecraft.hsp.server.api.World;
import com.andune.minecraft.hsp.server.api.YamlFile;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;


/** Class to process Essentials 2.9 data and convert it into our database.
 * 
 * @author morganm
 *
 */
public class Essentials29 extends BaseConverter
{
    final private Map<String, String> offlinePlayers = new HashMap<String,String>();

    @Override
    public String getConverterName() {
        return "Essentials 2.9";
    }
	
    @Override
	public int convert() throws Exception {
		File folder = plugin.getDataFolder();
		String parent = folder.getParent();
		File essentialsUserData = new File(parent + "/Essentials/userdata");
		
		if( !essentialsUserData.isDirectory() ) {
			log.warn("No essentials user directory found, skipping Home import");
			return 0;
		}
		
		loadOfflinePlayers();
		
		HomeDAO dao = storage.getHomeDAO();
		
		int convertedCount = 0;
		File[] files = essentialsUserData.listFiles();
		for(File file : files) {
		    YamlFile userData = factory.newYamlFile();
		    userData.load(file);
		    ConfigurationSection section = userData.getRootConfigurationSection();
		    
			Set<String> homes = section.getKeys("homes");
			if( homes != null && homes.size() > 0 ) {
				for(String home : homes) {
					String worldName = section.getString("homes."+home+".world");
					// if there's no world, this user doesn't have a home set.  Skip it.
					if( worldName == null )
						continue;
					World world = server.getWorld(worldName);
					if( world == null ) {
						log.warn("Essentials 2.9 converter: tried to convert home from world \"{}\", but no such world exists", worldName);
						continue;
					}
					
					Double x = section.getDouble("homes."+home+".x");
					Double y = section.getDouble("homes."+home+".y");
					Double z = section.getDouble("homes."+home+".z");
					Double yaw = section.getDouble("homes."+home+".yaw");
					Double pitch = section.getDouble("homes."+home+".pitch");
					
					String lowerCaseName = file.getName();
					lowerCaseName = lowerCaseName.substring(0, lowerCaseName.lastIndexOf('.'));
					
					// Essentials stores names in lowercase, HSP keeps proper case. So we
					// try to lookup the proper case name from the Bukkit offlinePlayers
					// map. If one doesn't exist, then we just use the lowercase name.
					String playerName = offlinePlayers.get(lowerCaseName);
					if( playerName == null )
						playerName = lowerCaseName;
					
		            Location l = factory.newLocation(world.getName(), x.doubleValue(), y.doubleValue(),
		                    z.doubleValue(), yaw.floatValue(), pitch.floatValue());
					
					HomeImpl hspHome = new HomeImpl();
					hspHome.setLocation(l);
					hspHome.setPlayerName(playerName);
					hspHome.setName(home);
					hspHome.setUpdatedBy("[Essentials29_Conversion]");
					// "home" is essentials version of HSP default home
					if( home.equals("home") )
						hspHome.setDefaultHome(true);
					
					try {
						dao.saveHome(hspHome);
						convertedCount++;
					}
					catch(StorageException e) {
						log.warn("StorageException attempting to convert Essentials 2.9 home", e);
					}
				}
			}
			
//			log.info("set home for player "+playerName);
		}
		
		return convertedCount;
	}
	
	/** Load Bukkit offline players into offlinePlayers hash, store key as
	 * lowercase name and value as proper case name.
	 * 
	 */
	private void loadOfflinePlayers() {
		OfflinePlayer[] players = server.getOfflinePlayers();
		for(OfflinePlayer player : players) {
			offlinePlayers.put(player.getName().toLowerCase(), player.getName());
		}
	}
}
