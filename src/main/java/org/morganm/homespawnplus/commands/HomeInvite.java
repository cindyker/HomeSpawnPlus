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
	public boolean execute(Player p, Command command, String[] args) {
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
			util.sendMessage(p, "No home named \""+homeName+"\" found. Try /homelist to view your homes.");
			return true;
		}
		
		String invitee = args[1];
		final Player onlinePlayer = Bukkit.getPlayer(invitee);
		final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(invitee);
		if( onlinePlayer == null && offlinePlayer == null ) {
			util.sendMessage(p, "No player named \""+invitee+"\" found.");
			return true;
		}
		if( onlinePlayer != null )
			invitee = onlinePlayer.getName();
		else if( offlinePlayer != null )
			invitee = offlinePlayer.getName();
		
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
					util.sendMessage(p, "Did not understand time format \""+lengthOfTime.toString()+"\"");
					util.sendMessage(p, "Try something like \"1d 4h\" or \"perm\" (for permanent)");
					return true;
				}
				else
					expireTimeAsString = General.getInstance().displayTimeString(timeInMilliseconds, false, false);
				
				expiresTime = System.currentTimeMillis() + timeInMilliseconds;
			}
		}
		
		org.morganm.homespawnplus.entity.HomeInvite homeInvite = new org.morganm.homespawnplus.entity.HomeInvite();
		homeInvite.setHome(home);
		homeInvite.setInvitedPlayer(invitee);
		if( expiresTime > System.currentTimeMillis() )
			homeInvite.setExpires(new Date(expiresTime));
		
		try {
			plugin.getStorage().getHomeInviteDAO().saveHomeInvite(homeInvite);
			util.sendMessage(p, "You have sent a home invite to player "+invitee+" to your home named "+homeName);
			if( expiresTime > 0 && expireTimeAsString != null )
				util.sendMessage(p, "Expire time for invite set to: "+expireTimeAsString);
			
			if( onlinePlayer != null )
				util.sendMessage(onlinePlayer, p.getName()+" has just sent you an invite to their home. Type /hil to list invites available to you.");
		}
		catch(StorageException e) {
			log.log(Level.WARNING, "Caught exception in command /homeinvite: "+e.getMessage(), e);
			util.sendMessage(p, "System error, please contact your administrator");
		}
		
		return true;
	}

}
