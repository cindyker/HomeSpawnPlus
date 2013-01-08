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

import java.util.Set;


import com.andune.minecraft.hsp.entity.PlayerSpawn;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/**
 * @author andune
 *
 */
public class PlayerSpawnDAOEBean implements PlayerSpawnDAO {
	private EbeanServer ebean;
	
	public PlayerSpawnDAOEBean(final EbeanServer ebean) {
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}

	@Override
	public PlayerSpawn findById(int id) {
		String q = "find spawn where id = :id";
		Query<PlayerSpawn> query = ebean.createQuery(PlayerSpawn.class, q);
		query.setParameter("id", id);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO#findByWorldAndPlayerName(java.lang.String, java.lang.String)
	 */
	@Override
	public PlayerSpawn findByWorldAndPlayerName(String world, String playerName) {
		String q = "find spawn where world = :world and player_name = :player_name";
		Query<PlayerSpawn> query = ebean.createQuery(PlayerSpawn.class, q);
		query.setParameter("world", world);
		query.setParameter("player_name", playerName);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO#findByPlayerName(java.lang.String)
	 */
	@Override
	public Set<PlayerSpawn> findByPlayerName(String playerName) {
		String q = "find spawn where player_name = :player_name";
		Query<PlayerSpawn> query = ebean.createQuery(PlayerSpawn.class, q);
		query.setParameter("player_name", playerName);
		
		return query.findSet();
	}

	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO#findAll()
	 */
	@Override
	public Set<PlayerSpawn> findAll() {
		return ebean.find(PlayerSpawn.class).findSet();
	}

	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO#save(com.andune.minecraft.hsp.entity.PlayerSpawn)
	 */
	@Override
	public void save(PlayerSpawn playerSpawn) throws StorageException {
        ebean.save(playerSpawn);
	}

	/* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO#delete(com.andune.minecraft.hsp.entity.PlayerSpawn)
	 */
	@Override
	public void delete(PlayerSpawn playerSpawn) throws StorageException {
        ebean.delete(playerSpawn);
	}

}
