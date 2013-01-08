/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
 */
/**
 * 
 */
package com.andune.minecraft.hsp.manager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.config.ConfigHomeInvites;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;

/** Class to manage temporary home invites (those that don't hit the database).
 * 
 * @author andune
 *
 */
@Singleton
public class HomeInviteManager {
	/* Data structure will support multiple invites to a single person, but
	 * right now we enforce only a single invite per person.
	 */
	private final Map<String, Map<String, Long>> tmpHomeInvites = new HashMap<String, Map<String, Long>>(10);
	
	private final ConfigHomeInvites config;
	private final Server server;
	private final Storage storage;
	
	@Inject
	public HomeInviteManager(ConfigHomeInvites config, Server server, Storage storage) {
	    this.config = config;
	    this.server = server;
	    this.storage = storage;
	}
	
	/** Return the invite timeout in milliseconds (ie. 30 seconds would return 30000)
	 * 
	 */
	public int getInviteTimeout() {
		return config.getTimeout() * 1000;
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
			homeName = "loc "+home.getLocation().shortLocationString();
		
		to.sendMessage( server.getLocalizedMessage(HSPMessages.TEMP_HOMEINVITE_RECEIVED,
				"who", from.getName(), "home", homeName, "time", getInviteTimeout()/1000) );
		
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
		
		return storage.getHomeDAO().findHomeById(id);
	}
	
	public void removeInvite(Player to) {
		Map<String, Long> inviteMap = tmpHomeInvites.get(to.getName());
		if( inviteMap != null ) {
			// assumes we only allow one outstanding invite right now
			inviteMap.clear();
		}
	}
}
