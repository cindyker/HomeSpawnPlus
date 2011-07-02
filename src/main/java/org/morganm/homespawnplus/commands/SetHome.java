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
	private static final String OTHER_SETHOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".sethome.others";
	
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String setter = p.getName();;
		String homeowner = setter;
		Location l = p.getLocation();

		if(args.length > 0 && !plugin.hasPermission(p, OTHER_SETHOME_PERMISSION))
		{
			// User is trying to set home for another user but they don't have permission
			util.sendMessage(p, "You don't have permission to do that.");
			return true;
		}

		// Setting home for different player
		if(args.length > 0)
			homeowner = args[0];

		util.setHome(homeowner, l, setter);
		if( homeowner != setter ) 
			util.sendMessage(p, "Home set successfully for player "+homeowner+"!");
		else
			util.sendMessage(p, "Home set successfully!");

		return true;
	}

}
