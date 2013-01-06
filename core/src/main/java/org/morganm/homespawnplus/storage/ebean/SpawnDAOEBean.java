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
package org.morganm.homespawnplus.storage.ebean;

import java.util.HashSet;
import java.util.Set;

import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.entity.SpawnImpl;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;

/**
 * @author morganm
 *
 */
public class SpawnDAOEBean implements SpawnDAO {
	private EbeanServer ebean;
	
	public SpawnDAOEBean(final EbeanServer ebean) {
		setEbeanServer(ebean);
	}
	
	public void setEbeanServer(final EbeanServer ebean) {
		this.ebean = ebean;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnByWorld(java.lang.String)
	 */
	@Override
	public Spawn findSpawnByWorld(String world) {
		return findSpawnByWorldAndGroup(world, Storage.HSP_WORLD_SPAWN_GROUP);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnByWorldAndGroup(java.lang.String, java.lang.String)
	 */
	@Override
	public Spawn findSpawnByWorldAndGroup(String world, String group) {
		String q = "find spawn where world = :world and group_name = :group";
		
		Query<SpawnImpl> query = ebean.createQuery(SpawnImpl.class, q);
		query.setParameter("world", world);
		query.setParameter("group", group);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnByName(java.lang.String)
	 */
	@Override
	public Spawn findSpawnByName(String name) {
		String q = "find spawn where name = :name";
		
		Query<SpawnImpl> query = ebean.createQuery(SpawnImpl.class, q);
		query.setParameter("name", name);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnById(int)
	 */
	@Override
	public Spawn findSpawnById(int id) {
		String q = "find spawn where id = :id";
		
		Query<SpawnImpl> query = ebean.createQuery(SpawnImpl.class, q);
		query.setParameter("id", id);
		
		return query.findUnique();
	}

    public Spawn getNewPlayerSpawn() {
        return findSpawnByName(NEW_PLAYER_SPAWN);
    }

	/** We make the assumption that there are relatively few spawns and group combinations,
	 * thus the easiest algorithm is simply to grab all the spawns and iterate through
	 * them for the valid group list.
	 * 
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#getSpawnDefinedGroups()
	 */
	@Override
	public java.util.Set<String> getSpawnDefinedGroups() {
		Set<String> groups = new HashSet<String>();
		Set<? extends Spawn> spawns = findAllSpawns();
		
		for(Spawn spawn : spawns) {
			String group = spawn.getGroup();
			if( group != null )
				groups.add(group);
		}
		
		return groups;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findAllSpawns()
	 */
	@Override
	public Set<? extends Spawn> findAllSpawns() {
		return ebean.find(SpawnImpl.class).findSet();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#saveSpawn(org.morganm.homespawnplus.entity.Spawn)
	 */
	@Override
	public void saveSpawn(Spawn spawn) {
        ebean.save(spawn);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#deleteSpawn(org.morganm.homespawnplus.entity.Spawn)
	 */
	@Override
	public void deleteSpawn(Spawn spawn) {
		ebean.delete(spawn);
	}

}
