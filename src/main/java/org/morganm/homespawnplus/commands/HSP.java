/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class HSP extends BaseCommand {

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !isEnabled() || !plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".admin") )
			return false;
		
		if( args.length < 1 ) {
			printUsage(p);
		}
		else if( args[0].startsWith("reloadc") || args[0].equals("rc") ) {
			boolean success = false;
			try {
				plugin.loadConfig();
				success = true;
			}
			catch(Exception e) {
				e.printStackTrace();
				util.sendMessage(p, "Error loading config data, not successful.  Check your server logs.");
			}
			
			if( success )
				util.sendMessage(p, "Config data reloaded.");
		}
		else if( args[0].startsWith("reloadd") || args[0].equals("rd") ) {
			// purge the existing cache
			plugin.getStorage().purgeCache();
			// now reload it by grabbing all of the objects
			plugin.getStorage().getAllHomes();
			plugin.getStorage().getAllSpawns();

			util.sendMessage(p, "Data cache purged and reloaded");
		}
		else {
			printUsage(p);
		}

		return true;
	}
	
	private void printUsage(Player p) {
		util.sendMessage(p, "Usage:");
		util.sendMessage(p, "/"+getCommandName()+" reloadconfig - reload config files");
		util.sendMessage(p, "/"+getCommandName()+" reloaddata - force reloading of plugin data from database");
	}

}
