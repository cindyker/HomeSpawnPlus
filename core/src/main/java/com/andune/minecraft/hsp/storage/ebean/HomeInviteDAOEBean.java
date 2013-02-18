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
package com.andune.minecraft.hsp.storage.ebean;

import java.util.HashSet;
import java.util.Set;


import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeInviteDAO;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;

/**
 * @author andune
 *
 */
public class HomeInviteDAOEBean implements HomeInviteDAO {
	private EbeanServer ebean;
	private Storage storage;
	
	public HomeInviteDAOEBean(final EbeanServer ebean, final Storage storage) {
	    this.storage = storage;
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	@Override
	public HomeInvite findHomeInviteById(int id) {
		String q = "find homeInvite where id = :id";

		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("id", id);
		
		return query.findUnique();
	}
	
	@Override
	public HomeInvite findInviteByHomeAndInvitee(Home home, String invitee) {
		String q = "find homeInvite where home = :home and lower(invitedPlayer) = lower(:invitee)";
		
		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("home", home.getId());
		query.setParameter("invitee", invitee);
		
		return query.findUnique();
	}

	@Override
	public Set<HomeInvite> findInvitesByHome(Home home) {
		String q = "find homeInvite where home = :home";
		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("home", home.getId());
		
		return query.findSet();
	}

	@Override
	public Set<HomeInvite> findAllAvailableInvites(String invitee) {
		String q = "find homeInvite where lower(invitedPlayer) = lower(:invitee)";
		Query<HomeInvite> query = ebean.createQuery(HomeInvite.class, q);
		query.setParameter("invitee", invitee);
		
		return query.findSet();
	}

	@Override
	public Set<HomeInvite> findAllOpenInvites(String player) {
		Set<HomeInvite> invites = new HashSet<HomeInvite>(5);
		
		// first find all homes for this player
		Set<? extends Home> homes = storage.getHomeDAO().findHomesByPlayer(player);
		if( homes == null || homes.size() == 0 )
			return invites;

		// then find all HomeInvites related to any of those homes
		for(Home home : homes) {
			Set<HomeInvite> homeInvites = findInvitesByHome(home);
			if( homeInvites != null )
				invites.addAll(homeInvites);
		}
		
		return invites;
	}

	@Override
	public void saveHomeInvite(HomeInvite homeInvite) {
		Transaction tx = ebean.beginTransaction();
        ebean.save(homeInvite, tx);
        tx.commit();
	}

	@Override
	public void deleteHomeInvite(HomeInvite homeInvite) throws StorageException {
		Transaction tx = ebean.beginTransaction();
		tx.setPersistCascade(false);
		ebean.delete(homeInvite, tx);
		tx.commit();
	}

	@Override
	public Set<HomeInvite> findAllHomeInvites() {
		return ebean.find(HomeInvite.class).findSet();
	}

}
