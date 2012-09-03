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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.morganm.homespawnplus.HomeSpawnUtils;


/** Converter for original SpawnControl.
 * 
 * @author morganm
 *
 */
public class SpawnControl implements Runnable {
	private static final Logger log = org.morganm.homespawnplus.HomeSpawnPlus.log;

	private final String logPrefix;
	private org.morganm.homespawnplus.HomeSpawnPlus plugin;
	private CommandSender initiatingPlayer;

	public SpawnControl(org.morganm.homespawnplus.HomeSpawnPlus plugin, CommandSender initiatingPlayer) {
		this.plugin = plugin;
		this.initiatingPlayer = initiatingPlayer;
		
		logPrefix = org.morganm.homespawnplus.HomeSpawnPlus.logPrefix;
	}
	
	private int convertHomes() {
        int convertedCount = 0;
        
		try
        {
			String db = "jdbc:sqlite:plugins/SpawnControl/spawncontrol.db";
    		Class.forName("org.sqlite.JDBC");
        	Connection conn = DriverManager.getConnection(db);
        	PreparedStatement ps = conn.prepareStatement("SELECT * FROM `players`");
            ResultSet rs = ps.executeQuery();

            HomeSpawnUtils util = plugin.getUtil();
            
            int consecutiveErrors = 0;
            while (rs.next()) {
            	// protect against a bunch of consecutive errors spamming the logfile
            	if( consecutiveErrors > 10 )
            		break;
            	
            	try {
            		String playerName = rs.getString("name");
            		String worldName = rs.getString("world");
            		World world = plugin.getServer().getWorld(worldName);
            		
            		Location l = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("r"), rs.getFloat("p"));
            		
            		util.setHome(playerName, l, "[SpawnControl_Conversion]", true, false);
            		convertedCount++;
            		
            		consecutiveErrors = 0;	// success! reset consecutiveErrors counter
            	}
            	catch(Exception e) {
            		log.warning(logPrefix + " error trying to process SQL row");
            		consecutiveErrors++;
            	}
            }
        	conn.close();
        	
        	if( consecutiveErrors > 10 )
        		log.warning(logPrefix + " conversion process aborted, too many consecutive errors");
        }
        catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[getPlayerData] DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
        
        return convertedCount;
	}
	
	public void run() {
		int homesConverted = convertHomes();
		if( initiatingPlayer != null )
			initiatingPlayer.sendMessage("Finished converting "+homesConverted+" homes");
	}
}
