/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Date;
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
		
		if( args.length < 2 ) {
//			util.sendLocalizedMessage(p, HSPMessages.CMDHELP_HOME_INVITE, "command", "hi");
//			util.sendMessage(p, command.getUsage());
			return false;
		}
		
		String homeName = args[0];
		org.morganm.homespawnplus.entity.Home home = plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(homeName, p.getName());
		if( home == null ) {
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_HOME_NOT_FOUND,
					"home", homeName);
			return true;
		}
		
		String invitee = args[1];
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
					expireTimeAsString = General.getInstance().displayTimeString(timeInMilliseconds, false, false);
				
				expiresTime = System.currentTimeMillis() + timeInMilliseconds;
			}
		}
		// it's just a temporary invite
		else {
			if( onlinePlayer == null ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_NO_PLAYER_FOUND,
						"player", invitee);
				return true;
			}
			
			plugin.getHomeInviteManager().sendHomeInvite(onlinePlayer, p, home);
			util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_INVITE_INVITE_SENT,
					"player", invitee, "home", home.getName());
		}
		
		org.morganm.homespawnplus.entity.HomeInvite homeInvite = new org.morganm.homespawnplus.entity.HomeInvite();
		homeInvite.setHome(home);
		homeInvite.setInvitedPlayer(invitee);
		if( expiresTime > System.currentTimeMillis() )
			homeInvite.setExpires(new Date(expiresTime));
		
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
