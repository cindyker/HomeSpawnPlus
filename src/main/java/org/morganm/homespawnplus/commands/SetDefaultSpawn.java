/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.storage.Storage;

/**
 * @author morganm
 *
 */
public class SetDefaultSpawn extends BaseCommand {

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		boolean localOnlyFlag = false;
		
		if( args.length < 1 ) {
			util.sendMessage(p, "You must specify the spawnName to set as default. Default spawn name is "+Storage.HSP_WORLD_SPAWN_GROUP);
			// TODO: show current default spawn
		}
		else {
			String spawnName = null;
			
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
			
			// TODO: do something to set the spawn anme
			org.morganm.homespawnplus.entity.Spawn spawn = util.getSpawnByName(spawnName);
			if( spawn != null ) {
				if( localOnlyFlag )
					spawn.setDefaultSpawn(true);
				else {
					// TODO: first blank out all globalSpawn flags.
					spawn.setGlobalDefaultSpawn(true);
				}
				
				plugin.getStorage().writeSpawn(spawn);
			}
		}
		
		return true;
	}

}
