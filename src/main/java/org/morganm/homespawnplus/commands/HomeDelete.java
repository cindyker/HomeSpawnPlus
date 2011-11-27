/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;

/**
 * @author morganm
 *
 */
public class HomeDelete extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"homed", "deletehome", "rmhome"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		org.morganm.homespawnplus.entity.Home home = null;
		String homeName = null;
		
		if( args.length > 0 ) {
			homeName = args[0];
			
			if( homeName.equals("<noname>") ) {
				Set<org.morganm.homespawnplus.entity.Home> homes = plugin.getStorage().getHomes(p.getWorld().getName(), p.getName());
				if( homes != null ) {
					for(org.morganm.homespawnplus.entity.Home h : homes) {
						if( h.getName() == null ) {
							home = h;
							break;
						}
					}
				}
			}
			else
				home = util.getHomeByName(p.getName(), homeName);
		}
		else
			home = util.getDefaultHome(p.getName(), p.getWorld().getName());

		if( home != null ) {
			// safety check to be sure we aren't deleting someone else's home with this command
			// (this shouldn't be possible since all checks are keyed to this player's name, but
			// let's be paranoid anyway)
			if( !p.getName().equals(home.getPlayerName()) ) {
				util.sendMessage(p, "ERROR: tried to delete another player's home; action not allowed.");
				log.warning(logPrefix + " ERROR: Shouldn't be possible! Player "+p.getName()+" tried to delete home for player "+home.getPlayerName());
			}
			else {
				plugin.getStorage().deleteHome(home);
				String msg = null;
				if( homeName != null )
					msg = "Home named "+homeName+" for player "+p.getName()+" deleted.";
				else
					msg = "Default home for player "+p.getName()+" on world "+p.getWorld().getName()+" deleted";
				util.sendMessage(p, msg);
			}
		}
		else if( homeName != null )
			util.sendMessage(p, "No home with name "+homeName+ " found to delete.");
		else
			util.sendMessage(p, "No home found to delete on world");
		
		return true;
	}

}
