/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.HomeInvite;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;
import org.morganm.homespawnplus.util.General;

/**
 * @author morganm
 *
 */
public class HomeInviteList extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hil"}; }
	
	@Override
	public boolean execute(Player p, Command command, String[] args) {
		if( !defaultCommandChecks(p) )
			return true;

		HomeInviteDAO dao = plugin.getStorage().getHomeInviteDAO();
		
		Set<HomeInvite> invites = dao.findAllOpenInvites(p.getName());
		if( invites != null && invites.size() > 0 ) {
			util.sendMessage(p, "Open invites for your homes:");
			for(HomeInvite invite : invites) {
				if( isExpired(invite) )
					continue;
				String homeName = invite.getHome().getName();
				util.sendMessage(p, "(id "+invite.getId()+") -> Invite for home "+homeName
						+" to player "+invite.getInvitedPlayer()
						+" [expires: "+(invite.getExpires() != null ?
								General.getInstance().displayTimeString(
										invite.getExpires().getTime()-System.currentTimeMillis(), true, "m") :
								"never")
						+"]"
						);
			}
		}
		else
			util.sendMessage(p, "You have no open invites to your homes");
		
		invites = dao.findAllAvailableInvites(p.getName());
		if( invites != null && invites.size() > 0 ) {
			util.sendMessage(p, "Open invites extended to you:");
			for(HomeInvite invite : invites) {
				if( isExpired(invite) )
					continue;
				String homeName = invite.getHome().getName();
				String playerName = invite.getHome().getPlayerName();
				util.sendMessage(p, "(id "+invite.getId()+") -> Invite to home "+homeName
						+" from player "+playerName
						+" [expires: "+(invite.getExpires() != null ?
								General.getInstance().displayTimeString(
										invite.getExpires().getTime()-System.currentTimeMillis(), true, "m") :
								"never")
						+"]"
						);
			}
		}
		else
			util.sendMessage(p, "You have no invites to other players homes");

		return true;
	}

	/** Check if a homeInvite is expired and if so, delete it. Any possible errors
	 * related to deleting it are ignored.
	 * 
	 * @param homeInvite
	 * @return true if the invite is expired, false if not
	 */
	private boolean isExpired(final HomeInvite homeInvite) {
		final boolean allowBedHomeInvites = plugin.getConfig().getBoolean(ConfigOptions.HOME_INVITE_ALLOW_BEDHOME, true);
		
		final org.morganm.homespawnplus.entity.Home home = homeInvite.getHome();
		// if the home no longer exists, clean up the invite
		if( home == null
				|| (home.getPlayerName() == null && home.getWorld() == null) ) {
			deleteHomeInvite(homeInvite);
			return true;
		}
		
		Date expires = homeInvite.getExpires();
		if( expires != null && expires.compareTo(new Date()) < 0 ) {
			deleteHomeInvite(homeInvite);
			return true;	// expired
		}
		// also check if it's a bedhome and we're not allowed to teleport to bedhomes
		else if( !allowBedHomeInvites && homeInvite.getHome().isBedHome() ) {
			deleteHomeInvite(homeInvite);
			return true;
		}
		else
			return false;	// not expired
	}
	
	private void deleteHomeInvite(final org.morganm.homespawnplus.entity.HomeInvite hi) {
		// it's expired, so delete it. we ignore any error here since it doesn't
		// affect the outcome of the rest of the command.
		try {
			plugin.getStorage().getHomeInviteDAO().deleteHomeInvite(hi);
		}
		catch(StorageException e) {
			log.log(Level.WARNING, "Caught exception: "+e.getMessage(), e);
		}
	}
}
