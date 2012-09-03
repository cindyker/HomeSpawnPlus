/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.manager.WarmupRunner;
import org.morganm.homespawnplus.strategy.EventType;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyResult;


/**
 * @author morganm
 *
 */
public class Home extends BaseCommand
{
	private static final String OTHER_WORLD_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.otherworld";
	private static final String NAMED_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.named";
	
	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_HOME_USAGE);
 	}
	
	@Override
	public boolean execute(final Player p, final org.bukkit.command.Command command, String[] args)
	{
		if( !isEnabled() || !hasPermission(p) )
			return true;
		
		debug.debug("home command called player=",p," args=",args);

		// this flag is used to determine whether the player influenced the outcome of /home
		// with an arg or whether it was purely determined by the default home strategy, so
		// that we know whether the OTHER_WORLD_PERMISSION perm needs to be checked
		boolean playerDirectedArg = false;
		final String warmupName = getWarmupName(null);
		String cooldownName = null;
		org.morganm.homespawnplus.entity.Home theHome = null;
		
		StrategyResult result = null;
		Location l = null;
		if( args.length > 0 ) {
			playerDirectedArg = true;
			String homeName = null;
			
			if( args[0].startsWith("w:") ) {
				if( !plugin.hasPermission(p, OTHER_WORLD_PERMISSION) ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION);
	    			return true;
				}
				
				String worldName = args[0].substring(2);
				theHome = util.getDefaultHome(p.getName(), worldName);
				if( theHome == null ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_HOME_ON_WORLD, "world", worldName);
					return true;
				}
			}
			else {
				if( !plugin.hasPermission(p, NAMED_HOME_PERMISSION) ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_NAMED_HOME_PERMISSION);
					return true;
				}
				
				result = util.getStrategyResult(EventType.NAMED_HOME_COMMAND, p, args[0]);
				theHome = result.getHome();
				l = result.getLocation();
			}

			// no location yet but we have a Home object? grab it from there
			if( l == null && theHome != null ) {
				l = theHome.getLocation();
				homeName = theHome.getName();
			}
			else
				homeName = args[0];
			
			cooldownName = getCooldownName("home-named", homeName);
			
			if( l == null ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_NAMED_HOME_FOUND, "name", homeName);
				return true;
			}
		}
		else {
			result = util.getStrategyResult(EventType.HOME_COMMAND, p);
			theHome = result.getHome();
			l = result.getLocation();
		}
		
		debug.debug("home command running cooldown check, cooldownName=",cooldownName);
		if( !cooldownCheck(p, cooldownName) )
			return true;
		
		final StrategyContext context;
		if( result != null )
			context = result.getContext();
		else
			context = null;
		
    	if( l != null ) {
    		// make sure it's on the same world, or if not, that we have
    		// cross-world home perms. We only evaluate this check if the
    		// player gave input for another world; admin-directed strategies
    		// always allow cross-world locations regardless of permissions.
    		if( playerDirectedArg && !p.getWorld().getName().equals(l.getWorld().getName()) &&
    				!plugin.hasPermission(p, OTHER_WORLD_PERMISSION) ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION);
    			return true;
    		}
    		
			if( hasWarmup(p, warmupName) ) {
	    		final Location finalL = l;
	    		final org.morganm.homespawnplus.entity.Home finalHome = theHome;
	    		final boolean finalIsNamedHome = playerDirectedArg;
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String cdName;
					private String wuName;
					
					public void run() {
						if( !canceled ) {
							util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", "home");
							doHomeTeleport(p, finalL, cdName, context,
									finalHome, finalIsNamedHome);
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
				}.setCooldownName(cooldownName).setWarmupName(warmupName));
			}
			else {
				doHomeTeleport(p, l, cooldownName, context, theHome, playerDirectedArg);
			}
    	}
    	else
			util.sendLocalizedMessage(p, HSPMessages.NO_HOME_FOUND);
    	
		return true;
	}
	
	/** Do a teleport to the home including costs, cooldowns and printing
	 * departure and arrival messages. Is used from both warmups and sync /home.
	 * 
	 * @param p
	 * @param l
	 */
	private void doHomeTeleport(Player p, Location l, String cooldownName,
			StrategyContext context, org.morganm.homespawnplus.entity.Home home,
			boolean isNamedHome)
	{
		String homeName = null;
		if( home != null )
			homeName = home.getName();
		
		if( applyCost(p, true, cooldownName) ) {
    		if( plugin.getConfig().getBoolean(ConfigOptions.TELEPORT_MESSAGES, false) ) {
    			if( home != null && home.isBedHome() )
	    			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_BED_TELEPORTING,
	    					"home", homeName);
    			else if( isNamedHome )
	    			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NAMED_TELEPORTING,
	    					"home", homeName);
    			else
	    			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_TELEPORTING,
	    					"home", homeName);
    		}
    		
    		util.teleport(p, l, TeleportCause.COMMAND, context);
		}
	}
	
	private String getWarmupName(String homeName) {
		return getCommandName();

		/* warmup per home doesn't even make sense. Silly.
		 * 
		if( homeName != null && plugin.getHSPConfig().getBoolean(ConfigOptions.WARMUP_PER_HOME, false) )
			return getCommandName() + "." + homeName;
		else
			return getCommandName();
			*/
	}
}
