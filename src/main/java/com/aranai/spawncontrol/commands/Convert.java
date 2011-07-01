/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.command.BaseCommand;
import com.aranai.spawncontrol.convert.Essentials23;
import com.aranai.spawncontrol.convert.SpawnControl;

/**
 * @author morganm
 *
 */
public class Convert extends BaseCommand {

	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			p.sendMessage("Usage: /convert [essentials|spawncontrol]");
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
