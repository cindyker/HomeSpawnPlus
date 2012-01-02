/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;


/**
 * @author morganm
 *
 */
public class SetSpawn extends BaseCommand
{
	@Override
	public String[] getCommandAliases() { return new String[] {"setglobalspawn"}; }

	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		if( args.length > 0 ) {
			util.setSpawn(args[0], p.getLocation(), p.getName());
			util.sendLocalizedMessage(p, HSPMessages.CMD_SETSPAWN_SET_NAMED_SUCCESS, "name", args[0]);
//			util.sendMessage(p, "Spawn "+args[0]+" set successfully.");
		}
		else {
			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to set global spawn.");
			util.setSpawn(p.getLocation(), p.getName());
			util.sendLocalizedMessage(p, HSPMessages.CMD_SETSPAWN_SET_SUCCESS);
//			util.sendMessage(p, "World spawn set successfully.");
		}
		
		return true;
	}
}
