/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;


/**
 * @author morganm
 *
 */
public class SetHome extends BaseCommand
{
//	private static final String OTHER_SETHOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + "command.sethome.others";
	
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( !applyCost(p) )
			return true;
		
		if( args.length > 0 ) {
			util.setNamedHome(p.getName(), p.getLocation(), args[0], p.getName());
			util.sendMessage(p, "Home \""+args[0]+"\" set successfully.");
		}
		else {
			util.setHome(p.getName(), p.getLocation(), p.getName());
			util.sendMessage(p, "Default home set successfully.");
		}

		return true;
	}

}
