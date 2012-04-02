/**
 * 
 */
package org.morganm.homespawnplus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.i18n.HSPMessages;

/** Class to manage temporary home invites (those that don't hit the database).
 * 
 * @author morganm
 *
 */
public class HomeInviteManager {
	private final HomeSpawnPlus plugin;
	/* Data structure will support multiple invites to a single person, but
	 * right now we enforce only a single invite per person.
	 */
	private final Map<String, Map<String, Long>> tmpHomeInvites = new HashMap<String, Map<String, Long>>(10);
	
	public HomeInviteManager(HomeSpawnPlus plugin) {
		this.plugin = plugin;
	}
	
	public int getInviteTimeout() {
		return plugin.getConfig().getInt(ConfigOptions.HOME_INVITE_TIMEOUT) * 1000;
	}
	
	/** Send a home invite to a player
	 * 
	 * @param to
	 * @param from
	 * @param home
	 * @return true if the invite was sent successfully, false if not
	 */
	public boolean sendHomeInvite(Player to, Player from, Home home) {
		// we only allow one invite at a time, so if there's already an invite,
		// we don't send another
		if( getInvitedHome(to) != null )
			return false;
		
		Map<String, Long> inviteMap = tmpHomeInvites.get(to.getName());
		if( inviteMap == null ) {
			inviteMap = new LinkedHashMap<String, Long>();
			tmpHomeInvites.put(to.getName(), inviteMap);
		}
		
		long timeout = System.currentTimeMillis() + getInviteTimeout();
		inviteMap.put(home.getPlayerName()+":"+home.getName(), Long.valueOf(timeout));

		plugin.getUtil().sendLocalizedMessage(to, HSPMessages.TEMP_HOMEINVITE_RECEIVED,
				"who", from.getName(), "home", home.getName(), "time", getInviteTimeout());
		
		return true;
	}
	
	/** If there is an open invite to a player, return it. Otherwise null
	 * is returned.
	 * 
	 * @param to
	 * @return
	 */
	public Home getInvitedHome(Player to) {
		Map<String, Long> inviteMap = tmpHomeInvites.get(to.getName());
		if( inviteMap == null )
			return null;
		
		// assumes we only allow one outstanding invite right now
		String inviteName = inviteMap.keySet().iterator().next();
		
		// if the invite has expired, remove it
		Long timeout = inviteMap.get(inviteName);
		if( System.currentTimeMillis() > timeout ) {
			removeInvite(to);
			return null;
		}
		
		int index = inviteName.indexOf(":");
		String playerFrom = inviteName.substring(0, index);
		String homeName = inviteName.substring(index+1, inviteName.length());
		
		return plugin.getUtil().getHomeByName(playerFrom, homeName);
	}
	
	public void removeInvite(Player to) {
		Map<String, Long> inviteMap = tmpHomeInvites.get(to.getName());
		if( inviteMap != null ) {
			// assumes we only allow one outstanding invite right now
			inviteMap.keySet().iterator().remove();
		}
	}
}
