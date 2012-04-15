/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.yaml.HomeDAOYaml;
import org.morganm.homespawnplus.storage.yaml.SpawnDAOYaml;
import org.morganm.homespawnplus.storage.yaml.StorageYaml;

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
			return false;
//			printUsage(p, command);
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
			plugin.getStorage().getHomeDAO().findAllHomes();
			plugin.getStorage().getSpawnDAO().findAllSpawns();

			util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RELOADED);
//			util.sendMessage(p, "Data cache purged and reloaded");
		}
		else if( args[0].equals("test") ) {
			org.morganm.homespawnplus.entity.Home home = plugin.getStorage().getHomeDAO().findDefaultHome("world", "morganm");
			p.sendMessage("Found home with id "+home.getId());
			org.morganm.homespawnplus.entity.Spawn spawn = plugin.getStorage().getSpawnDAO().findSpawnById(1);
			p.sendMessage("Found spawn with id "+spawn.getId());
			try {
				Float yaw = Float.valueOf(new Random(System.currentTimeMillis()).nextInt(360));
				File file = new File("plugins/HomeSpawnPlus/data.yml");
				
				HomeDAOYaml homeDAO = new HomeDAOYaml(file);
				homeDAO.load();
				home.setYaw(yaw);
				homeDAO.saveHome(home);
				
				home = homeDAO.findDefaultHome("world", "morganm");
				p.sendMessage("YML: Found home with yaw "+home.getYaw());

				SpawnDAOYaml spawnDAO = new SpawnDAOYaml(file);
				spawnDAO.load();
				spawn.setYaw(yaw);
				spawnDAO.saveSpawn(spawn);
				
				spawn = spawnDAO.findSpawnById(1);
				p.sendMessage("YML: Found spawn with yaw "+spawn.getYaw());
			}
			catch(Exception e) {
				p.sendMessage("Caught exception: "+e.getMessage());
				e.printStackTrace();
			}
		}
		else if( args[0].equals("test2") ) {
			Set<org.morganm.homespawnplus.entity.Home> allHomes = plugin.getStorage().getHomeDAO().findAllHomes();
			p.sendMessage("allHomes.size="+allHomes.size());
		}
		else if( args[0].startsWith("backup") ) {
			Storage storage = plugin.getStorage();

			File backupFile = new File(HomeSpawnPlus.YAML_BACKUP_FILE);
			if( backupFile.exists() )
				backupFile.delete();
			
			try {
				StorageYaml backupStorage = new StorageYaml(plugin, true, backupFile);
				backupStorage.initializeStorage();

				backupStorage.setDeferredWrites(true);
				for(org.morganm.homespawnplus.entity.Home o : storage.getHomeDAO().findAllHomes()) {
					debug.devDebug("backing up Home object id ",o.getId());
					backupStorage.getHomeDAO().saveHome(o);
				}
				for(org.morganm.homespawnplus.entity.Spawn o : storage.getSpawnDAO().findAllSpawns()) {
					debug.devDebug("backing up Spawn object id ",o.getId());
					backupStorage.getSpawnDAO().saveSpawn(o);
				}
				for(org.morganm.homespawnplus.entity.Player o : storage.getPlayerDAO().findAllPlayers()) {
					debug.devDebug("backing up Player object id ",o.getId());
					backupStorage.getPlayerDAO().savePlayer(o);
				}
				for(org.morganm.homespawnplus.entity.HomeInvite o : storage.getHomeInviteDAO().findAllHomeInvites()) {
					debug.devDebug("backing up HomeInvite object id ",o.getId());
					backupStorage.getHomeInviteDAO().saveHomeInvite(o);
				}

				backupStorage.flushAll();
	
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_BACKED_UP, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
				log.info(logPrefix+" Data backed up to file "+HomeSpawnPlus.YAML_BACKUP_FILE);
			}
			catch(StorageException e) {
				log.warning(logPrefix+" Error saving backup file"+e.getMessage());
				e.printStackTrace();
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_BACKUP_ERROR);
			}
		}
		else if( args[0].startsWith("restore") ) {
			if( args.length < 2 || (!"OVERWRITE".equals(args[1])
					&& !("me".equals(args[1]) && p instanceof ConsoleCommandSender)) ) {	// testing shortcut
				util.sendLocalizedMessage(p, HSPMessages.CMD_HSP_DATA_RESTORE_USAGE, "file", HomeSpawnPlus.YAML_BACKUP_FILE);
//				util.sendMessage(p, "In order to start restore you must send the command \"/hsp restore OVERWRITE\"");
//				util.sendMessage(p, "THIS WILL OVERWRITE EXISTING DATA and restore data from file "+HomeSpawnPlus.YAML_BACKUP_FILE);
			}
			else {
				File backupFile = new File(HomeSpawnPlus.YAML_BACKUP_FILE);
				if( backupFile.exists() ) {
					final Storage storage = plugin.getStorage();
					try {
						StorageYaml backupStorage = new StorageYaml(plugin, true, backupFile);
						backupStorage.initializeStorage();
						
						storage.deleteAllData();
						storage.setDeferredWrites(true);
						
						Set<org.morganm.homespawnplus.entity.Home> homes = backupStorage.getHomeDAO().findAllHomes();
						for(org.morganm.homespawnplus.entity.Home home : homes) {
							debug.devDebug("Restoring home ",home);
							home.setLastModified(null);
							storage.getHomeDAO().saveHome(home);
						}
						Set<org.morganm.homespawnplus.entity.Spawn> spawns = backupStorage.getSpawnDAO().findAllSpawns();
						for(org.morganm.homespawnplus.entity.Spawn spawn : spawns) {
							debug.devDebug("Restoring spawn ",spawn);
							spawn.setLastModified(null);
							storage.getSpawnDAO().saveSpawn(spawn);
						}
						Set<org.morganm.homespawnplus.entity.Player> players = backupStorage.getPlayerDAO().findAllPlayers();
						for(org.morganm.homespawnplus.entity.Player player : players) {
							debug.devDebug("Restoring player ",player);
							player.setLastModified(null);
							storage.getPlayerDAO().savePlayer(player);
						}
						Set<org.morganm.homespawnplus.entity.HomeInvite> homeInvites = backupStorage.getHomeInviteDAO().findAllHomeInvites();
						for(org.morganm.homespawnplus.entity.HomeInvite homeInvite : homeInvites) {
							debug.devDebug("Restoring homeInvite ",homeInvite);
							homeInvite.setLastModified(null);
							storage.getHomeInviteDAO().saveHomeInvite(homeInvite);
						}
						
						storage.flushAll();
					}
					catch(StorageException e) {
						util.sendMessage(p, "Caught exception: "+e.getMessage());
						log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
					}
					finally {
						storage.setDeferredWrites(false);
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
			return false;
//			printUsage(p, command);
		}

		return true;
	}
	
//	private void printUsage(CommandSender p, Command command) {
//		util.sendMessage(p, command.getUsage());
//		
////		util.sendMessage(p, "Usage:");
////		util.sendMessage(p, "/"+getCommandName()+" reloadconfig - reload config files");
////		util.sendMessage(p, "/"+getCommandName()+" reloaddata - force reloading of plugin data from database");
////		util.sendMessage(p, "/"+getCommandName()+" backup - backup database to a file");
////		util.sendMessage(p, "/"+getCommandName()+" restore - restore database from a file");
//	}

}
