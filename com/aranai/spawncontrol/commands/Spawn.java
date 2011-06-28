/**
 * 
 */
package com.aranai.spawncontrol.commands;

import org.bukkit.entity.Player;

import com.aranai.spawncontrol.SpawnControl;
import com.aranai.spawncontrol.command.BaseCommand;
import com.aranai.spawncontrol.config.ConfigOptions;
import com.nijikokun.bukkit.Permissions.Permissions;

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
		if( defaultCommandChecks(p) )
			return true;

		// defaults to SPAWN_GLOBAL if no config param is defined
		String spawnType = plugin.getConfig().getString(ConfigOptions.SETTING_SPAWN_BEHAVIOR, ConfigOptions.VALUE_SPAWN_GLOBAL);

		// Check permissions availability for group spawn
		if( !plugin.usePermissions && spawnType.equals(ConfigOptions.VALUE_SPAWN_GROUP) )
		{
			SpawnControl.log.warning("[SpawnControl] Spawn behavior set to 'group' but group support is not available. Using global spawn.");
			spawnType = ConfigOptions.VALUE_SPAWN_GLOBAL;
		}

		if( spawnType.equals(ConfigOptions.VALUE_SPAWN_HOME) ) {
			plugin.sendHome(p);
		}
		else if( spawnType.equals(ConfigOptions.VALUE_SPAWN_GLOBAL) ) {
			plugin.sendToSpawn(p);
		}
		else if( spawnType.equals(ConfigOptions.VALUE_SPAWN_GROUP) ) {
			if( !plugin.usePermissions ) {
				SpawnControl.log.warning("[SpawnControl] Spawn behavior set to 'group' but group support is not available. Using global spawn.");
				// Send player to global spawn
				plugin.sendToSpawn(p);
			}
			else
				plugin.sendToGroupSpawn(Permissions.Security.getGroup(p.getWorld().getName(), p.getName()), p);
		}
		
		SpawnControl.log.info("[SpawnControl] Sending player "+p.getName()+" to spawn ("+spawnType+").");
		return true;
	}
}
