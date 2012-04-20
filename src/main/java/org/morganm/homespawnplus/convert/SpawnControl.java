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
