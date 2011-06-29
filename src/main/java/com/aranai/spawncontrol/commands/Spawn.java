/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.command.BaseCommand;
import com.aranai.spawncontrol.config.ConfigOptions;

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
		System.out.println("Running spawn command");
		if( !defaultCommandChecks(p) )
			return true;

		// defaults to SPAWN_GLOBAL if no config param is defined
		String spawnType = plugin.getConfig().getString(ConfigOptions.SETTING_SPAWN_BEHAVIOR, ConfigOptions.VALUE_GLOBAL);

		// Check permissions availability for group spawn
		if( !plugin.isUsePermissions() && spawnType.equals(ConfigOptions.VALUE_GROUP) )
		{
			SpawnControl.log.warning(SpawnControl.logPrefix + " Spawn behavior set to 'group' but group support is not available. Using global spawn.");
			spawnType = ConfigOptions.VALUE_GLOBAL;
		}

		if( spawnType.equals(ConfigOptions.VALUE_HOME) )
			util.sendHome(p);
		else if( spawnType.equals(ConfigOptions.VALUE_GLOBAL) )
			util.sendToSpawn(p);
		else if( spawnType.equals(ConfigOptions.VALUE_GROUP) )
			util.sendToGroupSpawn(p);
		
		SpawnControl.log.info(SpawnControl.logPrefix + " Sending player "+p.getName()+" to spawn ("+spawnType+").");
		return true;
	}
}
