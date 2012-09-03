/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.manager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.morganm.homespawnplus.HomeSpawnPlus;
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
	
	/** Return the invite timeout in milliseconds (ie. 30 seconds would return 30000)
	 * 
	 */
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
		inviteMap.put(home.getPlayerName()+":"+home.getId(), Long.valueOf(timeout));

		String homeName = home.getName();
		if( homeName == null )
			homeName = "loc "+plugin.getUtil().shortLocationString(home);
		
		plugin.getUtil().sendLocalizedMessage(to, HSPMessages.TEMP_HOMEINVITE_RECEIVED,
				"who", from.getName(), "home", homeName, "time", getInviteTimeout()/1000);
		
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
		if( inviteMap == null || inviteMap.size() == 0 )
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
//		String playerFrom = inviteName.substring(0, index);
		String idString = inviteName.substring(index+1, inviteName.length());
		// will blow up with NumberFormatException if it's not an integer,
		// but that's OK because that should never be possible, so we want
		// it to blow up as a significant error condition.
		int id = Integer.valueOf(idString);
		
		return plugin.getStorage().getHomeDAO().findHomeById(id);
	}
	
	public void removeInvite(Player to) {
		Map<String, Long> inviteMap = tmpHomeInvites.get(to.getName());
		if( inviteMap != null ) {
			// assumes we only allow one outstanding invite right now
			inviteMap.clear();
		}
	}
}
