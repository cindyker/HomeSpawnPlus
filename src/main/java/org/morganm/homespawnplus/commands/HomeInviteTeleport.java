/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.WarmupRunner;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;

/** Cooldown, warmup and teleport logic structured similar to Home command.
 * 
 * @author morganm
 *
 */
public class HomeInviteTeleport extends BaseCommand {
	private static final String OTHER_WORLD_PERMISSION = HomeSpawnPlus.BASE_PERMISSION_NODE + ".command.homeinvitetp.otherworld";

	@Override
	public String[] getCommandAliases() { return new String[] {"hit", "hitp", "homeinvitetp"}; }
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.command.Command#execute(org.bukkit.entity.Player, org.bukkit.command.Command, java.lang.String[])
	 */
	@Override
	public boolean execute(final Player p, Command command, String[] args) {
		if( !isEnabled() || !hasPermission(p) )
			return true;
		
		if( args.length < 1 ) {
			util.sendMessage(p, command.getUsage());
			return true;
		}
		
		Location l = null;
		org.morganm.homespawnplus.entity.HomeInvite homeInvite = null;
		
		if( args.length == 1 ) {
			try {
				int id = Integer.parseInt(args[0]);
				homeInvite = plugin.getStorage().getHomeInviteDAO().findHomeInviteById(id);
			}
			catch(NumberFormatException e) {
				util.sendMessage(p, "Error: Expected id number, got \""+args[0]+"\"");
				util.sendMessage(p, command.getUsage());
			}
		}
		else if( args.length == 2 ) {
			// find the player
			Player targetPlayer = Bukkit.getPlayer(args[0]);
			OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(args[0]);
			String targetPlayerName = null;
			if( targetPlayer != null )
				targetPlayerName = targetPlayer.getName();
			else if( targetOfflinePlayer != null )
				targetPlayerName = targetOfflinePlayer.getName();
			else
				util.sendMessage(p, "Could not find player \""+args[0]+"\"");
			
			if( targetPlayerName != null ) {
				// now find the home with the name given for that player
				org.morganm.homespawnplus.entity.Home home = plugin.getStorage().getHomeDAO().findHomeByNameAndPlayer(args[1], targetPlayerName);
				
				// now look for the HomeInvite for that home with this player as the invitee
				homeInvite = plugin.getStorage().getHomeInviteDAO().findInviteByHomeAndInvitee(home, p.getName());
			}
		}
		else
			util.sendMessage(p, command.getUsage());
		
		if( homeInvite != null ) {
			// if we're not the invited player, we can't use this invite id
			if( !p.getName().equals(homeInvite.getInvitedPlayer()) )
				homeInvite = null;
			
			// check for expiry of the invite
			Date expires = homeInvite.getExpires();
			if( expires != null && expires.compareTo(new Date()) < 0 ) {
				// it's expired, so delete it. we ignore any error here since it doesn't
				// affect the outcome of the rest of the command.
				try {
					plugin.getStorage().getHomeInviteDAO().deleteHomeInvite(homeInvite);
				}
				catch(StorageException e) {
					log.log(Level.WARNING, "Caught exception: "+e.getMessage(), e);
				}
				
				homeInvite = null;
			}
			
			// if homeInvite is still non-null at this point, then we're allowed to use it
			if( homeInvite != null )
				l = homeInvite.getHome().getLocation();
		}
		
		
    	if( l != null ) {
    		// make sure it's on the same world, or if not, that we have cross-world home perms
    		if( !p.getWorld().getName().equals(l.getWorld().getName()) &&
    				!plugin.hasPermission(p, OTHER_WORLD_PERMISSION) ) {
				util.sendLocalizedMessage(p, HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION);
    			return true;
    		}
    		
    		final String cooldownName = getCooldownName("homeinvitetp", Integer.toString(homeInvite.getId()));
    		final String warmupName = "homeinvitetp";
    		debug.debug("homeInviteTeleport command running cooldown check, cooldownName=",cooldownName);
    		if( !cooldownCheck(p, cooldownName) )
    			return true;

			if( hasWarmup(p, warmupName) ) {
	    		final Location finalL = l;
	    		final String placeString = "home of "+ homeInvite.getHome().getPlayerName();
				doWarmup(p, new WarmupRunner() {
					private boolean canceled = false;
					private String cdName;
					private String wuName;
					
					public void run() {
						if( !canceled ) {
							util.sendLocalizedMessage(p, HSPMessages.CMD_WARMUP_FINISHED,
									"name", getWarmupName(), "place", placeString);
							if( applyCost(p, true, cdName) )
								p.teleport(finalL);
						}
					}

					public void cancel() {
						canceled = true;
					}

					public void setPlayerName(String playerName) {}
					public void setWarmupId(int warmupId) {}
					public WarmupRunner setCooldownName(String cd) { cdName = cd; return this; }
					public WarmupRunner setWarmupName(String warmupName) { wuName = warmupName; return this; }
					public String getWarmupName() { return wuName; }
				}.setCooldownName(cooldownName).setWarmupName(warmupName));
			}
			else {
				if( applyCost(p, true, cooldownName) )
					p.teleport(l);
			}
    	}
    	else
			util.sendLocalizedMessage(p, HSPMessages.NO_HOME_INVITE_FOUND);
    	
		return true;
	}

}
