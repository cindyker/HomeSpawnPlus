/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class Home extends BaseCommand
{
	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		SpawnControl.log.info("[SpawnControl] Attempting to send player "+p.getName()+" to home.");
		plugin.sendHome(p);
		
		return true;
	}

}
