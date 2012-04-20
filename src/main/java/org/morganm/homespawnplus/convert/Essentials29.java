/**
 * 
 */
package org.morganm.homespawnplus.convert;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
					
					String playerName = file.getName();
					playerName = playerName.substring(0, playerName.lastIndexOf('.'));
					
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
	
	public void run() {
		int homesConverted = convertHomes();
		if( initiatingPlayer != null )
			initiatingPlayer.sendMessage("Finished converting "+homesConverted+" homes");
	}
}
