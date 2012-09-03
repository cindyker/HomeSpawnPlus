/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;

/**
 * @author morganm
 *
 */
public class PermCheck extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"pc"}; }

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_PERMCHECK_USAGE);
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
	
	private boolean executePrivate(CommandSender sender, Command command, String[] args) {
		if( args.length < 1 ) { 
			util.sendMessage(sender, command.getUsage());
			return true;
		}
		
		String playerName = sender.getName();
		if( args.length > 1 )
			playerName = args[1];
		
		// let bukkit do "fuzzy match" and then get the exact name
		Player p = Bukkit.getPlayer(playerName);
		if( p != null ) {
			playerName = p.getName();
		}
		
		String world = "world";
		// set world arg if passed an argument
		if( args.length > 2 ) {
			world = args[2];
		}
		// otherwise if the player is logged in, default to the world they are currently in
		else if( p != null ) {
			world = p.getWorld().getName();
		}
		
		String permission = args[0];
		
		boolean result = plugin.getPermissionSystem().has(world, playerName, permission);
		
		HSPMessages msg = null;
		if( result )
			msg = HSPMessages.CMD_PERM_CHECK_TRUE;
		else
			msg = HSPMessages.CMD_PERM_CHECK_FALSE;
		
		util.sendLocalizedMessage(sender, msg,
				"permission", permission,
				"player", playerName,
				"world", world,
				"system", plugin.getPermissionSystem().getSystemInUseString());
		
		return true;
	}

}
