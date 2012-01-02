/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;


/**
 * @author morganm
 *
 */
public class SetFirstSpawn extends BaseCommand
{
	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Setting first-time player spawn.");
		util.setSpawn(ConfigOptions.VALUE_NEW_PLAYER_SPAWN, p.getLocation(), p.getName());
		util.sendLocalizedMessage(p, HSPMessages.CMD_SETFIRSTSPAWN_SET);
//		util.sendMessage(p, "First-time player spawn set successfully.");
		
		return true;
	}
}
