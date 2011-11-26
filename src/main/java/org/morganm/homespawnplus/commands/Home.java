/**
 * 
 */
package org.morganm.homespawnplus.commands;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.WarmupRunner;
import org.morganm.homespawnplus.command.BaseCommand;


/**
 * @author morganm
 *
 */
public class Home extends BaseCommand
{
	private static final String OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.others";
	private static final String DELETE_OTHER_HOME_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.home.delete.others";
	
	@Override
	public boolean execute(final Player p, final org.bukkit.command.Command command, String[] args)
	{
		if( !isEnabled() )
			return false;
		
		if( args.length > 0 ) {
			if( args[0].equals("help") ) {
				if(!hasPermission(p))
					return false;
				
				util.sendMessage(p, "Usage:");
				util.sendMessage(p, "  /home : go to your home on current world");
				util.sendMessage(p, "  /home world_name : go to your home on world \"world_name\"");
				util.sendMessage(p, "  /home delete : delete your home on current world");
				util.sendMessage(p, "  /home delete world_name : delete your home on world \"world_name\"");
				if( plugin.hasPermission(p, OTHER_HOME_PERMISSION) ) {
					util.sendMessage(p, "  /home player : go to \"player\"'s home on current world");
					util.sendMessage(p, "  /home player world_name : go to \"player\"'s home on world \"world_name\"");
					util.sendMessage(p, "  /home delete player world_name : delete \"player\"'s home on world \"world_name\"");
				}
				
				return true;
			}
			
			String world = p.getWorld().getName();
			String playerName = p.getName();
			
			World tmp = null;
			org.morganm.homespawnplus.entity.Home home = null;

			String[] newArgs;
			boolean isDeleting = false;
			if( args[0].equals("delete") ) {
				isDeleting = true;
				
				// shift args
				newArgs = new String[args.length-1];
				for(int i=1; i < args.length; i++)
					newArgs[i-1] = args[i];
			}
			else
			  newArgs = args;
			
			// can happen if they just type "/home delete"
			if( newArgs.length == 0 ) {
				if( !defaultCommandChecks(p) )
					return true;
				
				; 	// do nothing, just fall through with default player/world set
			}
			// if only 1 arg, it's either a player or a world; find out which
			else if( newArgs.length == 1 ) {
				if( !defaultCommandChecks(p) )
					return true;
				
				if( (tmp = plugin.getServer().getWorld(newArgs[0])) != null ) {
					world = tmp.getName();
					playerName = p.getName();
				}
				else
					playerName = newArgs[0];
			}
			// if there are 2 or more args, then they are trying to access another players home, so
			// check permission and find the home
			else if( newArgs.length > 1 ) {
				System.out.println("newArgs.length > 1");
				if( !plugin.hasPermission(p, OTHER_HOME_PERMISSION) )
					return true;
				
				if( !cooldownCheck(p) )
					return true;
				
				if( (tmp = plugin.getServer().getWorld(newArgs[1])) == null ) {
					util.sendMessage(p, "No such world found: "+newArgs[1]);
					return true;
				}
				else
					world = tmp.getName();

				playerName = newArgs[0];
				System.out.println("playerName = "+playerName+", world = "+world);
			}
			
			home = util.getHome(playerName, world);
			
			// didn't find an exact match and we have permission to use others homes?  try a best guess match
			if( home == null && plugin.hasPermission(p, OTHER_HOME_PERMISSION) )
				home = util.getBestMatchHome(playerName, world);
			
			if( home != null ) {
				if( isDeleting ) {
					if( p.getName().equals(home.getPlayerName()) || plugin.hasPermission(p, DELETE_OTHER_HOME_PERMISSION) ) {
						util.sendMessage(p, "Deleting home for player "+home.getPlayerName()+" on world \""+home.getWorld()+"\"");
						plugin.getStorage().removeHome(home);
					}
					else
						util.sendMessage(p, "You don't have permission to do that.");
				}
				else {
		    		// make sure the home is owned by the player, or if not, that we have other-home perms
		    		if( !p.getName().equals(home.getPlayerName()) &&
		    				!plugin.hasPermission(p, OTHER_HOME_PERMISSION) ) {
		    			util.sendMessage(p, "No permission to go to other players homes.");
		    			return true;
		    		}
		    		
					util.sendMessage(p, "Teleporting to player home for "+home.getPlayerName()+" on world \""+home.getWorld()+"\"");
					if( applyCost(p) )
						p.teleport(home.getLocation());
				}
			}
			else
				p.sendMessage("No home found for player "+playerName+" on world "+world);
		}
		// just someone typing /home - do normal checks and send them on their way
		else {
			if( !defaultCommandChecks(p) )
				return true;

			if( hasWarmup(p) ) {
				if ( !isWarmupPending(p) ) {
					warmupManager.startWarmup(p.getName(), getCommandName(), new WarmupRunner() {
						private boolean canceled = false;
						
						public void run() {
							if( !canceled ) {
								util.sendMessage(p, "Warmup \""+getCommandName()+"\" finished, teleporting home");
								if( applyCost(p) )
									util.sendHome(p);
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
			else {
				HomeSpawnPlus.log.info(HomeSpawnPlus.logPrefix + " Attempting to send player "+p.getName()+" to home.");
				if( applyCost(p) )
					util.sendHome(p);
			}
		}
		
		return true;
	}

}
