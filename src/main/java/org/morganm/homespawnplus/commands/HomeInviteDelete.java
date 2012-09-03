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
public class HomeInviteDelete extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hid"}; }

	@Override
	public String getUsage() {
		return	util.getLocalizedMessage(HSPMessages.CMD_HOME_INVITE_DELETE_USAGE);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			util.sendLocalizedMessage(p, HSPMessages.ERROR_ID_NUMBER_REQUIRED,
					"input", "null");
			return true;
		}
		
		int id = -1;
		try {
			id = Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {
			util.sendLocalizedMessage(p, HSPMessages.ERROR_ID_NUMBER_REQUIRED,
					"input", args[0]);
			return true;
		}
		org.morganm.homespawnplus.entity.HomeInvite hi = plugin.getStorage().getHomeInviteDAO().findHomeInviteById(id);
		
		// make sure we found an object and that the home is owned by the player
		if( hi != null && p.getName().equals(hi.getHome().getPlayerName()) ) {
			try {
				org.morganm.homespawnplus.entity.Home h = hi.getHome();
				String invitee = hi.getInvitedPlayer();
				plugin.getStorage().getHomeInviteDAO().deleteHomeInvite(hi);
				util.sendLocalizedMessage(p, HSPMessages.HOMEINVITE_DELETED,
						"id", id,
						"home", h.getName(),
						"invitee", invitee);
			}
			catch(StorageException e) {
				util.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
				log.log(Level.WARNING, "Caught exception in /"+getCommandName()+": "+e.getMessage(), e);
			}
		}
		else {
			util.sendLocalizedMessage(p, HSPMessages.HOMEINVITE_ID_NOT_FOUND,
					"id", args[0]);
		}
		
		return true;
	}

}
