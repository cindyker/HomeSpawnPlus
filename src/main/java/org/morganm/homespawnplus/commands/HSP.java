/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageYaml;

/**
 * @author morganm
 *
 */
public class HSP extends BaseCommand {
	private static final Logger log = HomeSpawnPlus.log;
	private String logPrefix;
	
	@Override
	public org.morganm.homespawnplus.command.Command setPlugin(HomeSpawnPlus plugin) {
		this.logPrefix = HomeSpawnPlus.logPrefix;
		return super.setPlugin(plugin);
	}

	@Override
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args) {
		return executePrivate(console, command, args);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		return executePrivate(p, command, args);
	}
	
	private boolean executePrivate(CommandSender p, Command command, String[] args) {
		if( !isEnabled() || !plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".admin") )
			return false;
		
		if( args.length < 1 ) {
			printUsage(p, command);
		}
		else if( args[0].startsWith("reloadc") || args[0].equals("rc") ) {
			boolean success = false;
			try {
				plugin.loadConfig();
				
				// also call hookWarmups, in case admin changed the warmup settings
				plugin.hookWarmups();
				
				success = true;
			}
			catch(Exception e) {
				e.printStackTrace();
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_ERROR_RELOADING);
//				util.sendMessage(p, "Error loading config data, not successful.  Check your server logs.");
			}
			
			if( success )
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_CONFIG_RELOADED);
//				util.sendMessage(p, "Config data reloaded.");
		}
		else if( args[0].startsWith("reloadd") || args[0].equals("rd") ) {
			// purge the existing cache
			plugin.getStorage().purgeCache();
			// now reload it by grabbing all of the objects
			plugin.getStorage().getAllHomes();
			plugin.getStorage().getAllSpawns();

			util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RELOADED);
//			util.sendMessage(p, "Data cache purged and reloaded");
		}
		else if( args[0].startsWith("backup") ) {
			Storage storage = plugin.getStorage();

			File backupFile = new File(HomeSpawnPlus.YAML_BACKUP_FILE);
			if( backupFile.exists() )
				backupFile.delete();
			
			StorageYaml backupStorage = new StorageYaml(backupFile);
			backupStorage.initializeStorage();
			
			backupStorage.addHomes(storage.getAllHomes());
			backupStorage.addSpawns(storage.getAllSpawns());
			backupStorage.addPlayers(storage.getAllPlayers());
			
			try {
				backupStorage.save();
	
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_BACKED_UP, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//				util.sendMessage(p, "Data backed up to file "+HomeSpawnPlus.YAML_BACKUP_FILE);
				log.info(logPrefix+" Data backed up to file "+HomeSpawnPlus.YAML_BACKUP_FILE);
			}
			catch(IOException e) {
				log.warning(logPrefix+" Error saving backup file"+e.getMessage());
				e.printStackTrace();
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_BACKUP_ERROR);
//				util.sendMessage(p, "There was an error writing the backup file, please check your server logs");
			}
		}
		else if( args[0].startsWith("restore") ) {
			if( args.length < 2 || !"OVERWRITE".equals(args[1]) ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_USAGE, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//				util.sendMessage(p, "In order to start restore you must send the command \"/hsp restore OVERWRITE\"");
//				util.sendMessage(p, "THIS WILL OVERWRITE EXISTING DATA and restore data from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
			}
			else {
				File backupFile = new File(HomeSpawnPlus.YAML_BACKUP_FILE);
				if( backupFile.exists() ) {
					StorageYaml backupStorage = new StorageYaml(backupFile);
					backupStorage.initializeStorage();
					
					Storage storage = plugin.getStorage();
					storage.deleteAllData();
					
					Set<org.morganm.homespawnplus.entity.Home> homes = backupStorage.getAllHomes();
					for(org.morganm.homespawnplus.entity.Home home : homes) {
						home.setLastModified(null);
						storage.writeHome(home);
					}
					Set<org.morganm.homespawnplus.entity.Spawn> spawns = backupStorage.getAllSpawns();
					for(org.morganm.homespawnplus.entity.Spawn spawn : spawns) {
						spawn.setLastModified(null);
						storage.writeSpawn(spawn);
					}
					Set<org.morganm.homespawnplus.entity.Player> players = backupStorage.getAllPlayers();
					for(org.morganm.homespawnplus.entity.Player player : players) {
						player.setLastModified(null);
						storage.writePlayer(player);
					}
					
					util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_SUCCESS, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//					util.sendMessage(p, "Existing data wiped and data restored from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
					log.info(logPrefix+" Existing data wiped and data restored from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
				}
				else
					util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_NO_FILE, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//					util.sendMessage(p, "Backup file not found, aborting restore (no data deleted). [file = "+HomeSpawnPlus.YAML_BACKUP_FILE+"]");
			}
		}
		else {
			printUsage(p, command);
		}

		return true;
	}
	
	private void printUsage(CommandSender p, Command command) {
		util.sendMessage(p, command.getUsage());
		
//		util.sendMessage(p, "Usage:");
//		util.sendMessage(p, "/"+getCommandName()+" reloadconfig - reload config files");
//		util.sendMessage(p, "/"+getCommandName()+" reloaddata - force reloading of plugin data from database");
//		util.sendMessage(p, "/"+getCommandName()+" backup - backup database to a file");
//		util.sendMessage(p, "/"+getCommandName()+" restore - restore database from a file");
	}

}
