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
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.HomeSpawnUtils;


/** Class to process Essentials 2.3 data and convert it into our database.
 * 
 * @author morganm
 *
 */
//@SuppressWarnings("deprecation")
public class Essentials23 implements Runnable {
	private static final Logger log = HomeSpawnPlus.log;
	
	private final String logPrefix;
	private HomeSpawnPlus plugin;
	private CommandSender initiatingPlayer;
	
	public Essentials23(HomeSpawnPlus plugin, CommandSender initiatingPlayer) {
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
		
		HomeSpawnUtils util = plugin.getUtil();
		World world = plugin.getServer().getWorld("world");
		
		int convertedCount = 0;
		File[] files = essentialsUserData.listFiles();
		for(File file : files) {
			YamlConfiguration userData = YamlConfiguration.loadConfiguration(file);
//			Configuration userData = new Configuration(file);
//			userData.load();
			
			String worldName = userData.getString("home.worlds.world.world");
			// if there's no world, this user doesn't have a home set.  Skip it.
			if( worldName == null )
				continue;
			Double x = userData.getDouble("home.worlds.world.x", 0);
			Double y = userData.getDouble("home.worlds.world.y", 0);
			Double z = userData.getDouble("home.worlds.world.z", 0);
			Double yaw = userData.getDouble("home.worlds.world.yaw", 0);
			Double pitch = userData.getDouble("home.worlds.world.pitch", 0);
			
			String playerName = file.getName();
			playerName = playerName.substring(0, playerName.lastIndexOf('.'));
			
			Location l = new Location(world, x.doubleValue(), y.doubleValue(),
					z.doubleValue(), yaw.floatValue(), pitch.floatValue());
			util.setHome(playerName, l, "[Essentials23_Conversion]", true, false);
			convertedCount++;
			
//			log.info("set home for player "+playerName);
		}
		
		return convertedCount;
	}
	
	public void run() {
		int homesConverted = convertHomes();
		if( initiatingPlayer != null )
			initiatingPlayer.sendMessage("Finished converting "+homesConverted+" homes");
	}
}
