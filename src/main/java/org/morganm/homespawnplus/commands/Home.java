/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.command.BaseCommand;


/**
 * @author morganm
 *
 */
public class Home extends BaseCommand
{
	private static final String OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".home.others";
	
	@Override
	public boolean execute(Player p, org.bukkit.command.Command command, String[] args)
	{
		// are they trying to go to someone else's home and do they have permission?
		if( args.length > 0 && plugin.hasPermission(p, OTHER_HOME_PERMISSION) ) {
			if( !cooldownManager.cooldownCheck(p, "home.others") )
				return true;
			
			String world;
			if( args.length > 1 )
				world = args[1];
			else
				world = p.getWorld().getName();
			
			org.morganm.homespawnplus.entity.Home home = util.getHome(args[0], world);
			
			// didn't find an exact match?  try a best guess match
			if( home == null )
				home = util.getBestMatchHome(args[0], world);
			
			if( home != null ) {
				util.sendMessage(p, "Teleporting to player home for "+home.getPlayerName()+" on world "+world);
				p.teleport(home.getLocation());
			}
			else
				p.sendMessage("No home found for player "+args[0]+" on world "+world);
		}
		// just someone typing /home - do normal checks and send them on their way
		else {
			if( !defaultCommandChecks(p) )
				return true;

			HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to send player "+p.getName()+" to home.");
			util.sendHome(p);
		}
		
		return true;
	}

}
