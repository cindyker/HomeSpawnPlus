/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;


/**
 * @author morganm
 *
 */
public class SetGroupSpawn extends BaseCommand
{
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_SETGROUPSPAWN_USAGE);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		if( args.length < 1 )
			return false;
		
		String group = args[0];
		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Setting group spawn for '"+group+"'.");
		util.setGroupSpawn(group, p.getLocation(), p.getName());
		util.sendMessage(p, "Group spawn for "+group+" set successfully!");
		
		return true;
	}
}
