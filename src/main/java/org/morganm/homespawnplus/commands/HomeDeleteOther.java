/**
 * 
 */
package org.morganm.homespawnplus.commands;

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
public class HomeDeleteOther extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hdo"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return false;

		if( args.length < 1 ) {
			util.sendMessage(p, command.getUsage());
			return true;
		}
		
		final String playerName = args[0];
		String worldName = null;
		String homeName = null;
		
		for(int i=1; i < args.length; i++) {
			if( args[i].startsWith("w:") ) {
				worldName = args[i].substring(2);
			}
			else {
				if( homeName != null ) {
					util.sendLocalizedMessage(p, HSPMessages.TOO_MANY_ARGUMENTS);
//					util.sendMessage(p,  "Too many arguments");
					return true;
				}
				homeName = args[i];
			}
		}
		
		if( worldName == null )
			worldName = p.getWorld().getName();
		
		org.morganm.homespawnplus.entity.Home home;
		if( homeName != null ) {
			home = plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(homeName, playerName);
		}
		else {
			home = util.getDefaultHome(playerName, worldName);
		}
		
		if( home != null ) {
			try {
				plugin.getStorage().getHomeDAO().deleteHome(home);
				if( homeName != null )
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_HOME_DELETED,
							"home", homeName, "player", playerName);
				else
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_DEFAULT_HOME_DELETED,
							"player", playerName, "world", worldName);
			}
			catch(StorageException e) {
				util.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
				log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
			}
		}
		else if( homeName != null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_HOME_FOUND,
					"home", homeName, "player", playerName);
		}
		else
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOMEDELETEOTHER_NO_DEFAULT_HOME_FOUND,
					"player", playerName, "world", worldName);
		
		return true;
	}

}
