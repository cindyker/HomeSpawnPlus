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
public class Spawn extends BaseCommand
{
	@Override
	public String[] getCommandAliases() { return new String[] {"globalspawn"}; }

	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		// defaults to SPAWN_GLOBAL if no config param is defined
		String spawnType = plugin.getConfig().getString(ConfigOptions.SETTING_SPAWN_BEHAVIOR, ConfigOptions.VALUE_GLOBAL);

		// Check permissions availability for group spawn
		if( !plugin.isUsePermissions() && spawnType.equals(ConfigOptions.VALUE_GROUP) )
		{
			HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " Spawn behavior set to 'group' but group support is not available. Using global spawn.");
			spawnType = ConfigOptions.VALUE_GLOBAL;
		}

		if( spawnType.equals(ConfigOptions.VALUE_HOME) )
			util.sendHome(p);
		else if( spawnType.equals(ConfigOptions.VALUE_WORLD) )
			util.sendToSpawn(p);
		else if( spawnType.equals(ConfigOptions.VALUE_GLOBAL) )
			util.sendToGlobalSpawn(p);
		else if( spawnType.equals(ConfigOptions.VALUE_GROUP) )
			util.sendToGroupSpawn(p);
		else
			HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " ERROR; unknown spawn type "+spawnType);
		
		HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Sending player "+p.getName()+" to spawn ("+spawnType+").");
		return true;
	}
}
