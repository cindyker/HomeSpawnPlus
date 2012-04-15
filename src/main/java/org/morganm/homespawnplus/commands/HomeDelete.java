/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;

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
			
			if( args[0].startsWith("w:") ) {
				String worldName = args[0].substring(2);
				home = util.getDefaultHome(p.getName(), worldName);
				if( home == null ) {
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_HOME_ON_WORLD, "world", worldName);
//					util.sendMessage(p,  "No home on world \""+worldName+"\" found.");
					return true;
				}
			}
			else if( homeName.equals("<noname>") ) {
				Set<org.morganm.homespawnplus.entity.Home> homes = plugin.getStorage()
						.getHomeDAO().findHomesByWorldAndPlayer(p.getWorld().getName(), p.getName());
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
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETE_ERROR_DELETING_OTHER_HOME);
//				util.sendMessage(p, "ERROR: tried to delete another player's home; action not allowed.");
				log.warning(logPrefix + " ERROR: Shouldn't be possible! Player "+p.getName()+" tried to delete home for player "+home.getPlayerName());
			}
			else {
				try {
					plugin.getStorage().getHomeDAO().deleteHome(home);
					if( homeName != null )
						util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETE_HOME_DELETED, "name", homeName);
					else
						util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETE_DEFAULT_HOME_DELETED);
				}
				catch(StorageException e) {
					util.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
					log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
				}
			}
		}
		else if( homeName != null )
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETE_NO_HOME_FOUND, "name", homeName);
//			util.sendMessage(p, "No home with name "+homeName+ " found to delete.");
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETE_NO_DEFAULT_HOME_FOUND);
//			util.sendMessage(p, "No default home found to delete on current world");
		
		return true;
	}

}
