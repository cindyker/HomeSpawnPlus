/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class SpawnDelete extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"spawnd", "deletespawn", "rmspawn"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		org.morganm.homespawnplus.entity.Spawn spawn = null;
		
		if( args.length < 1 ) {
			util.sendMessage(p, command.getUsage());
			return true;
		}
		
		int id = -1;
		try {
			id = Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {}
		if( id != -1 )
			spawn = plugin.getStorage().getSpawnById(id);
		
		if( spawn == null )
			spawn = plugin.getStorage().getSpawnByName(args[0]);
		
		if( spawn == null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_NO_SPAWN_FOUND,
					"name", args[0]);
//			util.sendMessage(p, "No spawn found for name or id: "+args[0]);
			return true;
		}
		
		plugin.getStorage().deleteSpawn(spawn);
		util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_SPAWN_DELETED,
				"name", args[0]);
//		util.sendMessage(p, "Spawn "+args[0]+" deleted.");
		return true;
	}

}
