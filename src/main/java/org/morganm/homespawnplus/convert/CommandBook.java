/**
 * 
 */
package org.morganm.homespawnplus.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;


/** Class to process Commandbook home data and convert it into our storage format.
 * 
 * @author morganm
 *
 */
public class CommandBook implements Runnable {
	private static final Logger log = HomeSpawnPlus.log;
	
	private final String logPrefix;
	private HomeSpawnPlus plugin;
	private CommandSender initiatingPlayer;
	
	public CommandBook(HomeSpawnPlus plugin, CommandSender initiatingPlayer) {
		this.plugin = plugin;
		this.initiatingPlayer = initiatingPlayer;
		
		logPrefix = HomeSpawnPlus.logPrefix;
	}
	
	private int convertHomes() throws IOException {
		// keep track of player names as we convert their first home, allows us to
		// set the first home we run into as the default home.
		Set<String> playerNames = new HashSet<String>();
		
		File folder = plugin.getDataFolder();
		String parent = folder.getParent();
		File commandBookHomeData = new File(parent + "/CommandBook/homes.csv");
		
		if( !commandBookHomeData.isFile() ) {
			log.warning(logPrefix + " No CommandBook homes.csv found, skipping home import");
			return 0;
		}
		
		HomeDAO dao = plugin.getStorage().getHomeDAO();
		
		int convertedCount = 0;
		BufferedReader br = new BufferedReader(new FileReader(commandBookHomeData));
		String line = null;
		while( (line = br.readLine()) != null ) {
			line = line.replaceAll("\"", "");
			String[] arr = line.split(",");

			String homeName = arr[0];
			String worldName = arr[1];
			String playerName = arr[2];
			Double x = Double.parseDouble(arr[3]);
			Double y = Double.parseDouble(arr[4]);
			Double z = Double.parseDouble(arr[5]);
			Double pitch = Double.parseDouble(arr[6]);
			Double yaw = Double.parseDouble(arr[7]);
			
			World world = Bukkit.getWorld(worldName);
			if( world == null ) {
				log.warning("CommandBook converter: tried to convert home from world \""+worldName+"\", but no such world exists");
				continue;
			}
			
			Location l = new Location(world, x.doubleValue(), y.doubleValue(),
					z.doubleValue(), yaw.floatValue(), pitch.floatValue());
			
			Home hspHome = new Home();
			hspHome.setLocation(l);
			hspHome.setPlayerName(playerName);
			hspHome.setName(homeName);
			hspHome.setUpdatedBy("[CommandBook_Conversion]");
			
			// first home we find for a player is considered the default home
			if( !playerNames.contains(playerName) ) {
				hspHome.setDefaultHome(true);
				playerNames.add(playerName);
			}
			
			try {
				dao.saveHome(hspHome);
				convertedCount++;
			}
			catch(StorageException e) {
				log.log(Level.WARNING, "StorageException attempting to convert CommandBook home", e);
			}
		}
		
		return convertedCount;
	}
	
	public void run() {
		try {
			int homesConverted = convertHomes();
			if( initiatingPlayer != null )
				initiatingPlayer.sendMessage("Finished converting "+homesConverted+" homes");
		}
		catch(IOException e) {
			log.log(Level.WARNING, "I/O error trying to convert CommmandBook homes", e);
			initiatingPlayer.sendMessage("Error converting CommandBook homes, check your server.log");
		}
	}
}
