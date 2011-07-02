/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.convert.Essentials23;
import org.morganm.homespawnplus.convert.SpawnControl;


/**
 * @author morganm
 *
 */
public class HSPConvert extends BaseCommand {

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !isEnabled() || !plugin.hasPermission(p, HomeSpawnPlus.BASE_PERMISSION_NODE+".admin") )
			return false;
		
		if( args.length < 1 ) {
			p.sendMessage("Usage: /"+getCommandName()+" [essentials|spawncontrol]");
		}
		else if( args[0].equalsIgnoreCase("essentials") ) {
			p.sendMessage("Starting Essentials conversion");
			Essentials23 converter = new Essentials23(plugin, p);
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, converter);
		}
		else if( args[0].equalsIgnoreCase("spawncontrol") ) {
			p.sendMessage("Starting SpawnControl conversion");
			SpawnControl converter = new SpawnControl(plugin, p);
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, converter);
		}
		
		return true;
	}

}
