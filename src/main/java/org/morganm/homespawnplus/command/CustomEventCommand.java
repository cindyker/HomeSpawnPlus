/**
 * 
 */
package org.morganm.homespawnplus.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.WarmupRunner;
import org.morganm.homespawnplus.strategy.StrategyEngine;

/**
 * @author morganm
 *
 */
public class CustomEventCommand extends BaseCommand {
	
	public String getEvent() {
		return super.getStringParam("event");
	}
	
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		String event = getEvent();
		if( event == null ) {
			util.sendMessage(p, "There was an error, please report it to your administrator");
			log.warning("CustomChainCommand event is null, cannot execute");
			return true;
		}
		
		// pass along an argument, if there was one
		String arg = null;
		if( args.length > 0 )
			arg = args[0];
		
		StrategyEngine engine = plugin.getStrategyEngine();
		Location l = engine.getStrategyLocation(event, p, arg);
		if( l == null ) {
			util.sendLocalizedMessage(p, HSPMessages.NO_LOCATION_FOUND);
			return true;
		}
		
		final String cooldownName = getCooldownName(getCommandName(), null);
		
		if( hasWarmup(p, cooldownName) ) {	// warmup name is same as cooldown
    		final Location finalL = l;
			doWarmup(p, new WarmupRunner() {
				private boolean canceled = false;
				private String cdName;
				private String wuName;
				
				public void run() {
					if( !canceled ) {
						util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
								"name", getWarmupName(), "place", util.shortLocationString(finalL));
						if( applyCost(p, true, cdName) )
				    		util.teleport(p, finalL, TeleportCause.COMMAND);
					}
				}

				public void cancel() {
					canceled = true;
				}

				public void setPlayerName(String playerName) {}
				public void setWarmupId(int warmupId) {}
				public WarmupRunner setCooldownName(String cd) { cdName = cd; return this; }
				public WarmupRunner setWarmupName(String warmupName) { wuName = warmupName; return this; }
				public String getWarmupName() { return wuName; }
			}.setCooldownName(cooldownName).setWarmupName(cooldownName));
		}
		else {
			if( applyCost(p, true, cooldownName) )
	    		util.teleport(p, l, TeleportCause.COMMAND);
		}

		return true;
	}
}
