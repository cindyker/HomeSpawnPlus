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
public class GroupSpawn extends BaseCommand
{
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( defaultCommandChecks(p) )
			return true;
		
		// Get group spawn for player
		SpawnControl.log.info(SpawnControl.logPrefix + " Attempting to send player "+p.getName()+" to group spawn.");
		util.sendToGroupSpawn(p);
		
		return true;
	}

}
