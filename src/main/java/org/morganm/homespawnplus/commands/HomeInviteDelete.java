/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public class HomeInviteDelete extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hid"}; }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
			return false;
		}
		
		int id = Integer.parseInt(args[0]);
		org.morganm.homespawnplus.entity.HomeInvite hi = plugin.getStorage().getHomeInviteDAO().findHomeInviteById(id);
		
		// make sure we found an object and that the home is owned by the player
		if( hi != null && p.getName().equals(hi.getHome().getPlayerName()) ) {
			try {
				org.morganm.homespawnplus.entity.Home h = hi.getHome();
				String invitee = hi.getInvitedPlayer();
				plugin.getStorage().getHomeInviteDAO().deleteHomeInvite(hi);
				util.sendMessage(p,  "Invite id #"+id+" was deleted [home name="+h.getName()+", invitee="+invitee+"]");
			}
			catch(StorageException e) {
				util.sendMessage(p, "There was a system error, please contact your administrator");
				log.log(Level.WARNING, "Caught exception in /"+getCommandName()+": "+e.getMessage(), e);
			}
		}
		else {
			util.sendMessage(p, "No invite with the id "+args[0]+" found. Type /hil to see your invites");
		}
		
		return true;
	}

}
