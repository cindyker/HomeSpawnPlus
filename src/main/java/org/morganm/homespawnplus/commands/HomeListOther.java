/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class HomeListOther extends BaseCommand {
	private HomeList homeListCommand;

	@Override
	public org.morganm.homespawnplus.command.Command setPlugin(HomeSpawnPlus plugin) {
		homeListCommand = new HomeList();
		homeListCommand.setPlugin(plugin);
		
		return super.setPlugin(plugin);
	}

	@Override
	public boolean execute(ConsoleCommandSender console, org.bukkit.command.Command command, String[] args) {
		return executePrivate(console, command, args);
	}

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		return executePrivate(p, command, args);
	}
	
	private boolean executePrivate(CommandSender p, Command command, String[] args) {
		String player = null;
		String world = "all";
		
		if( args.length < 1 ) {
			util.sendMessage(p, command.getUsage());
			return true;
		}
		
		player = args[0];
		if( args.length > 1 )
			world = args[1];
		
		return homeListCommand.executeCommand(p, player, world);
	}

}
