/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;


/**
 * @author morganm
 *
 */
public class SetFirstSpawn extends BaseCommand
{
	@Override
	public String[] getCommandAliases() { return new String[] {"setglobalspawn"}; }

	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to set first-time player spawn.");
		util.setSpawn(ConfigOptions.STRATEGY_SPAWN_NEW_PLAYER, p.getLocation(), p.getName());
		util.sendMessage(p, "First-time player spawn set successfully.");
		
		return true;
	}
}
