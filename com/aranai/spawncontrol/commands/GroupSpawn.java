/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.command.BaseCommand;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * @author morganm
 *
 */
public class GroupSpawn extends BaseCommand
{
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( defaultCommandChecks(p) )
			return true;
		
		// Get group spawn for player
		String group = Permissions.Security.getGroup(p.getWorld().getName(), p.getName());
		SpawnControl.log.info("[SpawnControl] Attempting to send player "+p.getName()+" to group spawn.");
		plugin.sendToGroupSpawn(group, p);
		
		return true;
	}

}
