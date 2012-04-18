/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.WarmupRunner;
import org.morganm.homespawnplus.strategy.EventType;


/**
 * @author morganm
 *
 */
public class GroupSpawn extends BaseCommand
{
	private static final String OTHER_GROUPSPAWN_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.groupspawn.named";
	
	@Override
	public String[] getCommandAliases() { return new String[] {"gs"}; }
	
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !isEnabled() || !hasPermission(p) )
			return true;

		String cooldownName = "groupspawn";
		
		Location l = null;
		if( args.length > 0 ) {
			if( plugin.hasPermission(p, OTHER_GROUPSPAWN_PERMISSION) ) {
				org.morganm.homespawnplus.entity.Spawn spawn = util.getGroupSpawn(args[0], p.getWorld().getName());
				cooldownName = getCooldownName("groupspawn-named", args[0]);
				if( spawn != null )
					l = spawn.getLocation();
				
				if( l == null ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,
							"group", args[0]);
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
			l = plugin.getStrategyEngine().getStrategyLocation(EventType.GROUPSPAWN_COMMAND, p);
//			StrategyInfo spawnInfo = new StrategyInfo();
//			spawnInfo.spawnEventType = ConfigOptions.SETTING_GROUPSPAWN_CMD_BEHAVIOR;
//			l = util.getStrategyLocation(p, spawnInfo);
		}

		if( !cooldownCheck(p, cooldownName) )
			return true;
    	
		if( l == null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_GROUPSPAWN_NO_GROUPSPAWN_FOR_GROUP,
					"group", plugin.getPlayerGroup(p.getWorld().getName(), p.getName()));
			
//			util.sendMessage(p, "No groupSpawn found for your group: "+plugin.getPlayerGroup(p.getWorld().getName(), p.getName()));
			return true;
		}

		if( hasWarmup(p) ) {
    		final Location finalL = l;
			doWarmup(p, new WarmupRunner() {
				private boolean canceled = false;
				private String wuName = getCommandName();

				public void run() {
					if( !canceled ) {
						util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
								"name", getWarmupName(), "place", "group spawn");
//						util.sendMessage(p, "Warmup \""+getWarmupName()+"\" finished, teleporting to group spawn");
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
		
		return true;
	}

}
