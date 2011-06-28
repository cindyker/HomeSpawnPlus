/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class SetHome extends BaseCommand
{
	@SuppressWarnings("static-access")
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String setter = p.getName();;
		String homeowner = setter;
		Location l = p.getLocation();

		if(args.length > 0 && !plugin.hasPermission(p, plugin.BASE_PERMISSION_NODE+".sethome.proxy"))
		{
			// User is trying to set home for another user but they don't have permission
			p.sendMessage("You don't have permission to do that.");
			return true;
		}

		// Setting home for different player
		if(args.length > 0)
			homeowner = args[0];

		if(plugin.setHome(homeowner, l, setter))
			p.sendMessage("Home set successfully!");
		else
			p.sendMessage("Could not set Home!");

		return true;
	}

}
