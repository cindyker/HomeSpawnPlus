/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;


/**
 * @author morganm
 *
 */
public class GroupSpawn extends BaseCommand
{
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
//		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to send player "+p.getName()+" to group spawn.");
		
		if( applyCost(p) )
			util.sendToGroupSpawn(p);
		
		return true;
	}

}
