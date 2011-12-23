/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.SpawnInfo;
import org.morganm.homespawnplus.WarmupRunner;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;


/**
 * @author morganm
 *
 */
public class GroupSpawn extends BaseCommand
{
	@Override
	public String[] getCommandAliases() { return new String[] {"gs"}; }
	
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		SpawnInfo spawnInfo = new SpawnInfo();
		spawnInfo.spawnEventType = ConfigOptions.SETTING_GROUPSPAWN_CMD_BEHAVIOR;
		final Location l = util.getStrategyLocation(p, spawnInfo);
		
		if( l == null ) {
			util.sendMessage(p, "No groupSpawn found for your group: "+plugin.getPlayerGroup(p.getWorld().getName(), p.getName()));
			return true;
		}

		if( hasWarmup(p) ) {
			doWarmup(p, new WarmupRunner() {
				private boolean canceled = false;
				private String wuName = getCommandName();

				public void run() {
					if( !canceled ) {
						util.sendMessage(p, "Warmup \""+getWarmupName()+"\" finished, teleporting to group spawn");
						if( applyCost(p, true) )
							p.teleport(l);
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
		
		return true;
	}

}
