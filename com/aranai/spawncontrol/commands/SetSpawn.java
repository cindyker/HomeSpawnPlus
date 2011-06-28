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
public class SetSpawn extends BaseCommand
{
	@Override
	public String[] getCommandAliases() { return new String[] {"setglobalspawn"}; }

	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		SpawnControl.log.info("[SpawnControl] Attempting to set global spawn.");
		if(plugin.setSpawn(p.getLocation(), p.getName())) {
			p.sendMessage("Global spawn set successfully!");
		}
		else {
			p.sendMessage("Could not set global spawn.");
		}
		
		return true;
	}
}
