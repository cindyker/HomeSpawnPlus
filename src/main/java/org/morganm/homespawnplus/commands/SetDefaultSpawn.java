/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class SetDefaultSpawn extends BaseCommand {

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
//		boolean localOnlyFlag = false;
		
		if( args.length < 1 ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPECIFY_NAME);
//			util.sendMessage(p, "You must specify the spawnName to set as default.");
		}
		else {
			String spawnName = args[0];
			
			/*
			if( args[0].equals("-l") ) {
				if( args.length < 2 ) {
					util.sendMessage(p, "You must specify the spawnName to set as global default. Single world spawn is named "+Storage.HSP_WORLD_SPAWN_GROUP);
				}
				else {
					localOnlyFlag = true;
					spawnName = args[1];
				}
			}
			else {
				spawnName = args[0];
			}
			*/
			
			org.morganm.homespawnplus.entity.Spawn spawn = util.getSpawnByName(spawnName);
			if( spawn != null ) {
				Location l = spawn.getLocation();
				util.setSpawn(l, p.getName());
				util.sendLocalizedMessage(p, HSPMessages.CMD_SETDEFAULTSPAWN_SPAWN_CHANGED,
						"name", spawn.getName(), "location", util.shortLocationString(l));
				
//				util.sendMessage(p, "Default spawn changed to "+spawn.getName()+" at location ["+util.shortLocationString(l)+"]");
				
				/*
				if( localOnlyFlag )
				else {
					// TODO: first blank out all globalSpawn flags.
					spawn.setGlobalDefaultSpawn(true);
				}
				
				plugin.getStorage().writeSpawn(spawn);
				*/
			}
		}
		
		return true;
	}

}
