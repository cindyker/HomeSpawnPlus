/**
 * 
 */
package org.morganm.homespawnplus.convert;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
	private Player initiatingPlayer;
	
	public Essentials23(HomeSpawnPlus plugin, Player initiatingPlayer) {
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
