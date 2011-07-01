/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class SetHome extends BaseCommand
{
	private static final String OTHER_SETHOME_PERMISSION = SpawnControl.BASE_PERMISSION_NODE + ".sethome.others";
	
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
			p.sendMessage("You don't have permission to do that.");
			return true;
		}

		// Setting home for different player
		if(args.length > 0)
			homeowner = args[0];

		util.setHome(homeowner, l, setter);
		p.sendMessage("Home set successfully!");

		return true;
	}

}
