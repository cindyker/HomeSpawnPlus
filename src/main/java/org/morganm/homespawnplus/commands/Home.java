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
public class Home extends BaseCommand
{
//	private static final String OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.others";
//	private static final String DELETE_OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.delete.others";
	private static final String OTHER_WORLD_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.otherworld";
	private static final String NAMED_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.named";
	
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
		
		Location l = null;
		if( args.length > 0 ) {
			playerDirectedArg = true;
			org.morganm.homespawnplus.entity.Home home = null;
			String homeName = null;
			
			if( args[0].startsWith("w:") ) {
				if( !plugin.hasPermission(p, OTHER_WORLD_PERMISSION) ) {
	    			util.sendMessage(p, "No permission to go to homes in other worlds.");
	    			return true;
				}
				
				String worldName = args[0].substring(2);
				home = util.getDefaultHome(p.getName(), worldName);
				if( home == null ) {
					util.sendMessage(p,  "No home on world \""+worldName+"\" found.");
					return true;
				}
			}
			else {
				if( !plugin.hasPermission(p, NAMED_HOME_PERMISSION) ) {
	    			util.sendMessage(p, "No permission to go to named homes");
					return true;
				}
				
				SpawnInfo spawnInfo = new SpawnInfo();
				spawnInfo.spawnEventType = ConfigOptions.SETTING_HOME_NAMED_CMD_BEHAVIOR;
				spawnInfo.argData = args[0];
				l = util.getStrategyLocation(p, spawnInfo);
//				home = util.getHomeByName(p.getName(), args[0]);
			}
				
			if( home != null ) {
				l = home.getLocation();
				homeName = home.getName();
			}
			else
				homeName = args[0];
			
			cooldownName = getCooldownName("home-named", homeName);
			
			if( l == null ) {
				util.sendMessage(p,  "No home named \""+homeName+"\" found.");
				return true;
			}
		}
		else {
			SpawnInfo spawnInfo = new SpawnInfo();
			spawnInfo.spawnEventType = ConfigOptions.SETTING_HOME_CMD_BEHAVIOR;
			l = util.getStrategyLocation(p, spawnInfo);
		}
		
		debug.debug("home command running cooldown check, cooldownName=",cooldownName);
		if( !cooldownCheck(p, cooldownName) )
			return true;
		
    	if( l != null ) {
    		// make sure it's on the same world, or if not, that we have cross-world home perms
    		// we only evaluate this check if the player gave input for another world; admin-directed
    		// strategies always allow cross-world locations regardless of permissions.
    		if( playerDirectedArg && !p.getWorld().getName().equals(l.getWorld().getName()) &&
    				!plugin.hasPermission(p, OTHER_WORLD_PERMISSION) ) {
    			util.sendMessage(p, "No permission to go to homes in other worlds.");
    			return true;
    		}
    		
			if( hasWarmup(p, warmupName) ) {
	    		final Location finalL = l;
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String cdName;
					private String wuName;
					
					public void run() {
						if( !canceled ) {
							util.sendMessage(p, "Warmup \""+wuName+"\" finished, teleporting to home");
							if( applyCost(p, true, cdName) )
								p.teleport(finalL);
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
				if( applyCost(p, true, cooldownName) )
					p.teleport(l);
			}
    	}
    	else
    		util.sendMessage(p, "No home found");
    	
		return true;
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
