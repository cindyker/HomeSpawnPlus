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
	private static final String OTHER_SPAWN_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.spawn.named";
	
	/*
	@Override
	public String[] getCommandAliases() { return new String[] {"globalspawn"}; }
	*/

	@Override
	public boolean execute(final Player p, final org.bukkit.command.Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		Location l = null;
		if( args.length > 0 ) {
			if( plugin.hasPermission(p, OTHER_SPAWN_PERMISSION) ) {
				org.morganm.homespawnplus.entity.Spawn spawn = util.getSpawnByName(args[0]);
				if( spawn != null )
					l = spawn.getLocation();
				
				if( l == null ) {
					util.sendMessage(p,  "No spawn \""+args[0]+"\" found.");
					return true;
				}
			}
			else {
				util.sendMessage(p,  "No permission");
			}
		}
		else {
			SpawnInfo spawnInfo = new SpawnInfo();
			spawnInfo.spawnEventType = ConfigOptions.SETTING_SPAWN_CMD_BEHAVIOR;
			l = util.getStrategyLocation(p, spawnInfo);
		}
    	
    	// TODO: need to add group permission checks
    	
    	if( l != null ) {
			if( hasWarmup(p) ) {
	    		final Location finalL = l;
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;

					public void run() {
						if( !canceled ) {
							util.sendMessage(p, "Warmup \""+getCommandName()+"\" finished, teleporting to spawn");
							if( applyCost(p, true) )
								p.teleport(finalL);
						}
					}

					public void cancel() {
						canceled = true;
					}

					public void setPlayerName(String playerName) {}
					public void setWarmupId(int warmupId) {}
				});
			}
			else {
				if( applyCost(p, true) )
					p.teleport(l);
			}
    	}
    	else
    		HomeSpawnPlus.log.warning(HomeSpawnPlus.logPrefix + " ERROR; not able to find a spawn location");
    	
		return true;
	}
}
