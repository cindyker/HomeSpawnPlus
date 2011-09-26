/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.SpawnInfo;
import org.morganm.homespawnplus.WarmupRunner;
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
	public boolean execute(final Player p, final org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

    	SpawnInfo spawnInfo = new SpawnInfo();
    	spawnInfo.spawnEventType = ConfigOptions.SETTING_SPAWN_CMD_BEHAVIOR;
    	final Location l = util.getSpawnLocation(p, spawnInfo);
    	
    	// TODO: need to add group permission checks
    	
    	if( l != null ) {
			if( hasWarmup(p) ) {
				if ( !isWarmupPending(p) ) {
					warmupManager.startWarmup(p.getName(), getCommandName(), new WarmupRunner() {
						private boolean canceled = false;
						
						public void run() {
							if( !canceled ) {
								util.sendMessage(p, "Warmup \""+getCommandName()+"\" finished, teleporting to spawn");
								p.teleport(l);
							}
						}

						public void cancel() {
							canceled = true;
						}
						
						public void setPlayerName(String playerName) {}
						public void setWarmupId(int warmupId) {}
					});
					
					util.sendMessage(p, "Warmup "+getCommandName()+" started, you must wait "+
							warmupManager.getWarmupTime(getCommandName())+" seconds.");
				}
				else
					util.sendMessage(p, "Warmup already pending for "+getCommandName());
			}
			else
				p.teleport(l);
    	}
    	else
    		HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " ERROR; not able to find a spawn location");
    	
    	/*
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
		*/
    	
		return true;
	}
}
