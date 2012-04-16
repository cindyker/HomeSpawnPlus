/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Date;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.util.General;

/**
 * @author morganm
 *
 */
public class HomeInvite extends BaseCommand {

	@Override
	public String[] getCommandAliases() { return new String[] {"hi"}; }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(final Player p, final Command command, final String[] args) {
		if( !defaultCommandChecks(p) )
			return true;
		
		if( args.length < 1 ) {
//			util.sendLocalizedMessage(p, HSPMessages.CMDHELP_HOME_INVITE, "command", "hi");
//			util.sendMessage(p, command.getUsage());
			return false;
		}
		
		org.morganm.homespawnplus.entity.Home home = null;
		if( args.length > 1 ) {
			String homeName = args[1];
			
			// try id # first
			try {
				int id = Integer.parseInt(homeName);
				home = plugin.getStorage().getHomeDAO().findHomeById(id);
				// the name on the home and this player's name must match, else we ignore it
				if( !p.getName().equals(home.getPlayerName()) )
					home = null;
				if( home != null )
					homeName = home.getName();
			}
			catch(NumberFormatException e) { /* do nothing, we don't care */ }
			
			// then try by name
			if( home == null )
				home = plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(homeName, p.getName());
			
			// if we didn't find a home, report error message to player
			if( home == null ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_HOME_NOT_FOUND,
						"home", homeName);
				return true;
			}
		}
		// if they didn't pass a 2nd arg, then we try some assumptions to find a home
		else {
			// try the default home on the world the player is in
			home = util.getDefaultHome(p.getName(), p.getWorld());
			
			// if that didn't work, try to see if they only have 1 total home, and if so, use that one
			if( home == null ) {
				Set<org.morganm.homespawnplus.entity.Home> homes = plugin.getStorage().getHomeDAO().findHomesByPlayer(p.getName());
				if( homes.size() == 1 )
					home = homes.iterator().next();
			}
			
			if( home == null ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_NO_HOME_SPECIFIED);
				return true;
			}
		}
		
		String invitee = args[0];
		final Player onlinePlayer = Bukkit.getPlayer(invitee);
		final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(invitee);
		if( onlinePlayer == null && offlinePlayer == null ) {
			util.sendLocalizedMessage(p, HSPMessages.PLAYER_NOT_FOUND,
					"player", invitee);
			return true;
		}
		if( onlinePlayer != null )
			invitee = onlinePlayer.getName();
		else if( offlinePlayer != null )
			invitee = offlinePlayer.getName();
		
		final boolean allowBedHomeInvites = plugin.getConfig().getBoolean(ConfigOptions.HOME_INVITE_ALLOW_BEDHOME, true);
		if( !allowBedHomeInvites && home.isBedHome() ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_NOT_ALLOWED,
					"home", home.getName());
			return true;
		}
		
		long expiresTime = 0;		// default to never expires
		String expireTimeAsString = null;
		if( args.length > 2 ) {
			if( args[2].equals("forever") || args[2].startsWith("perm") )
				expiresTime = 0;	// forever
			else {
				StringBuffer lengthOfTime = new StringBuffer();
				for(int i=2; i < args.length; i++) {
					if( lengthOfTime.length() > 0 )
						lengthOfTime.append(" ");
					lengthOfTime.append(args[i]);
				}
				long timeInMilliseconds = General.getInstance().parseTimeInput(lengthOfTime.toString());
				if( timeInMilliseconds < 60000 ) {		// minimum time is 1 minute
					util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_BAD_TIME,
							"badTime", lengthOfTime.toString());
					return true;
				}
				else
					expireTimeAsString = General.getInstance().displayTimeString(timeInMilliseconds, false, null);
				
				expiresTime = System.currentTimeMillis() + timeInMilliseconds;
			}
		}
		// it's just a temporary invite
		else {
			if( onlinePlayer == null ) {
				util.sendLocalizedMessage(p, HSPMessages.PLAYER_NOT_FOUND,
						"player", invitee);
				return true;
			}
			
			// if there is a cost and we don't have the money, do not pass go
			if( !applyCost(p, true, getCommandName()) )
				return true;

			plugin.getHomeInviteManager().sendHomeInvite(onlinePlayer, p, home);
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_INVITE_SENT,
					"player", invitee, "home", home.getName());
			return true;
		}
		
		// if we get here, this is not a temporary invite, so we need to check permissions
		if( !plugin.hasPermission(p, getCommandPermissionNode()+".permanent") ) {
			util.sendLocalizedMessage(p, HSPMessages.NO_PERMISSION);
			return true;
		}
		
		org.morganm.homespawnplus.entity.HomeInvite homeInvite = new org.morganm.homespawnplus.entity.HomeInvite();
		homeInvite.setHome(home);
		homeInvite.setInvitedPlayer(invitee);
		if( expiresTime > System.currentTimeMillis() )
			homeInvite.setExpires(new Date(expiresTime));
		
		// if there is a cost and we don't have the money, do not pass go
		if( !applyCost(p, true, getCommandName()) )
			return true;
		
		try {
			plugin.getStorage().getHomeInviteDAO().saveHomeInvite(homeInvite);
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_INVITE_SENT,
					"player", invitee, "home", home.getName());
			if( expiresTime > 0 && expireTimeAsString != null )
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_EXPIRE_TIME_SET,
						"expire", expireTimeAsString);
			
			if( onlinePlayer != null )
				util.sendLocalizedMessage(onlinePlayer, HSPMessages.CMD_HOME_INVITE_INVITE_RECEIVED,
						"player", p.getName());
		}
		catch(StorageException e) {
			log.log(Level.WARNING, "Caught exception in command /homeinvite: "+e.getMessage(), e);
			util.sendLocalizedMessage(onlinePlayer, HSPMessages.GENERIC_ERROR);
		}
		
		return true;
	}

}
