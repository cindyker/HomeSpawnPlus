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
import org.morganm.homespawnplus.i18n.HSPMessages;


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
		if( !isEnabled() || !hasPermission(p) )
			return true;

		String cooldownName = "spawn";
		
		Location l = null;
		if( args.length > 0 ) {
			if( plugin.hasPermission(p, OTHER_SPAWN_PERMISSION) ) {
				org.morganm.homespawnplus.entity.Spawn spawn = util.getSpawnByName(args[0]);
				cooldownName = getCooldownName("spawn-named", args[0]);
				if( spawn != null )
					l = spawn.getLocation();
				
				if( l == null ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWN_NO_SPAWN_FOUND, "name", args[0]);
//					util.sendMessage(p,  "No spawn \""+args[0]+"\" found.");
					return true;
				}
			}
			else {
				util.sendLocalizedMessage(p, HSPMessages.NO_PERMISSION);
//				util.sendMessage(p,  "No permission");
			}
		}
		else {
			SpawnInfo spawnInfo = new SpawnInfo();
			spawnInfo.spawnEventType = ConfigOptions.SETTING_SPAWN_CMD_BEHAVIOR;
			l = util.getStrategyLocation(p, spawnInfo);
		}
    	
		if( !cooldownCheck(p, cooldownName) )
			return true;
    	
    	if( l != null ) {
			if( hasWarmup(p) ) {
	    		final Location finalL = l;
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String wuName = getCommandName();

					public void run() {
						if( !canceled ) {
							util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", "spawn");
//							util.sendMessage(p, "Warmup \""+getWarmupName()+"\" finished, teleporting to spawn");
							if( applyCost(p, true) )
								p.teleport(finalL);
						}
					}

					public void cancel() {
						canceled = true;
					}

					public void setPlayerName(String playerName) {}
					public void setWarmupId(int warmupId) {}
					public WarmupRunner setWarmupName(String warmupName) { wuName = warmupName; return this; }
					public String getWarmupName() { return wuName; }
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
