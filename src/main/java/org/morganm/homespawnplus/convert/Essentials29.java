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
package org.morganm.homespawnplus.convert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;


/** Class to process Essentials 2.9 data and convert it into our database.
 * 
 * @author morganm
 *
 */
//@SuppressWarnings("deprecation")
public class Essentials29 implements Runnable {
	private static final Logger log = HomeSpawnPlus.log;
	
	private final String logPrefix;
	private HomeSpawnPlus plugin;
	private CommandSender initiatingPlayer;
	final private Map<String, String> offlinePlayers = new HashMap<String,String>();
	
	public Essentials29(HomeSpawnPlus plugin, CommandSender initiatingPlayer) {
		this.plugin = plugin;
		this.initiatingPlayer = initiatingPlayer;
		
		logPrefix = HomeSpawnPlus.logPrefix;
	}
	
	private int convertHomes() {
		File folder = plugin.getDataFolder();
		String parent = folder.getParent();
		File essentialsUserData = new File(parent + "/Essentials/userdata");
		
		if( !essentialsUserData.isDirectory() ) {
			log.warning(logPrefix + " No essentials user directory found, skipping Home import");
			return 0;
		}
		
		loadOfflinePlayers();
		
		HomeDAO dao = plugin.getStorage().getHomeDAO();
		
		int convertedCount = 0;
		File[] files = essentialsUserData.listFiles();
		for(File file : files) {
			YamlConfiguration userData = YamlConfiguration.loadConfiguration(file);
			
			ConfigurationSection section = userData.getConfigurationSection("homes");
			Set<String> homes = null;
			if( section != null )
				homes = section.getKeys(false);
			
			if( homes != null && homes.size() > 0 ) {
				for(String home : homes) {
					String worldName = userData.getString("homes."+home+".world");
					// if there's no world, this user doesn't have a home set.  Skip it.
					if( worldName == null )
						continue;
					World world = Bukkit.getWorld(worldName);
					if( world == null ) {
						log.warning("Essentials 2.9 converter: tried to convert home from world \""+worldName+"\", but no such world exists");
						continue;
					}
					
					Double x = userData.getDouble("homes."+home+".x", 0);
					Double y = userData.getDouble("homes."+home+".y", 0);
					Double z = userData.getDouble("homes."+home+".z", 0);
					Double yaw = userData.getDouble("homes."+home+".yaw", 0);
					Double pitch = userData.getDouble("homes."+home+".pitch", 0);
					
					String lowerCaseName = file.getName();
					lowerCaseName = lowerCaseName.substring(0, lowerCaseName.lastIndexOf('.'));
					
					// Essentials stores names in lowercase, HSP keeps proper case. So we
					// try to lookup the proper case name from the Bukkit offlinePlayers
					// map. If one doesn't exist, then we just use the lowercase name.
					String playerName = offlinePlayers.get(lowerCaseName);
					if( playerName == null )
						playerName = lowerCaseName;
					
					Location l = new Location(world, x.doubleValue(), y.doubleValue(),
							z.doubleValue(), yaw.floatValue(), pitch.floatValue());
					
					Home hspHome = new Home();
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
						log.log(Level.WARNING, "StorageException attempting to convert Essentials 2.9 home", e);
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
		OfflinePlayer[] players = Bukkit.getOfflinePlayers();
		for(OfflinePlayer player : players) {
			offlinePlayers.put(player.getName().toLowerCase(), player.getName());
		}
	}
	
	public void run() {
		int homesConverted = convertHomes();
		if( initiatingPlayer != null )
			initiatingPlayer.sendMessage("Finished converting "+homesConverted+" homes");
	}
}
