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
import org.morganm.homespawnplus.convert.CommandBook;
import org.morganm.homespawnplus.convert.Essentials23;
import org.morganm.homespawnplus.convert.Essentials29;
import org.morganm.homespawnplus.convert.SpawnControl;


/**
 * @author morganm
 *
 */
public class HSPConvert extends BaseCommand {

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
		if( !isEnabled() || !plugin.hasPermission(sender, HomeSpawnPlus.BASE_PERMISSION_NODE+".admin") )
			return false;
		
		Runnable converter = null;
		
		if( args.length < 1 ) {
			sender.sendMessage("Usage: /"+getCommandName()+" [essentials|spawncontrol|commandbook]");
		}
		else if( args[0].equalsIgnoreCase("commandbook") ) {
			sender.sendMessage("Starting CommandBook conversion");
			converter = new CommandBook(plugin, sender);
		}
		else if( args[0].equalsIgnoreCase("essentials") ) {
			sender.sendMessage("Starting Essentials 2.9+ conversion");
			converter = new Essentials29(plugin, sender);
		}
		else if( args[0].equalsIgnoreCase("essentials23") ) {
			sender.sendMessage("Starting Essentials 2.3 conversion");
			converter = new Essentials23(plugin, sender);
		}
		else if( args[0].equalsIgnoreCase("spawncontrol") ) {
			sender.sendMessage("Starting SpawnControl conversion");
			converter = new SpawnControl(plugin, sender);
		}
		else {
			sender.sendMessage("Unknown conversion type: "+args[0]);
		}
		
		if( converter != null )
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, converter);
		
		return true;
	}

}
