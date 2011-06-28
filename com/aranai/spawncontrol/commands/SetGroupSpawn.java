/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class SetGroupSpawn extends BaseCommand
{
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		String group = null;

		if(!(args.length > 0)) {
			p.sendMessage("Command format: /setgroupspawn [group]");
		}
		else {
			group = args[0];
			SpawnControl.log.info(SpawnControl.logPrefix + " Setting group spawn for '"+group+"'.");
			util.setGroupSpawn(group, p.getLocation(), p.getName());
			p.sendMessage("Group spawn for "+group+" set successfully!");
		}
		
		return true;
	}
}
