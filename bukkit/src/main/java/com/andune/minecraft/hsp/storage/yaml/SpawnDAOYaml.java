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
package com.andune.minecraft.hsp.storage.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.storage.yaml.serialize.SerializableSpawn;

/**
 * @author morganm
 *
 */
public class SpawnDAOYaml extends AbstractDAOYaml<Spawn, SerializableSpawn> implements SpawnDAO {
	private static final String CONFIG_SECTION = "spawns";
	
	public SpawnDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public SpawnDAOYaml(final File file) {
		this(file, null);
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnByWorld(java.lang.String)
	 */
	@Override
	public Spawn findSpawnByWorld(String world) {
		Spawn spawn = null;
		
		Set<Spawn> spawns = findAllSpawns();
		if( spawns != null && spawns.size() > 0 ) {
			for(Spawn s: spawns) {
				if( world.equals(s.getWorld()) ) {
					spawn = s;
					break;
				}
			}
		}
		
		return spawn;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnByWorldAndGroup(java.lang.String, java.lang.String)
	 */
	@Override
	public Spawn findSpawnByWorldAndGroup(String world, String group) {
		Spawn spawn = null;
		
		Set<Spawn> spawns = findAllSpawns();
		if( spawns != null && spawns.size() > 0 ) {
			for(Spawn s: spawns) {
				if( world.equals(s.getWorld()) && group.equals(s.getGroup()) ) {
					spawn = s;
					break;
				}
			}
		}
		
		return spawn;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnByName(java.lang.String)
	 */
	@Override
	public Spawn findSpawnByName(String name) {
		Spawn spawn = null;
		
		Set<Spawn> spawns = findAllSpawns();
		if( spawns != null && spawns.size() > 0 ) {
			for(Spawn s: spawns) {
				if( name.equals(s.getName()) ) {
					spawn = s;
					break;
				}
			}
		}
		
		return spawn;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnById(int)
	 */
	@Override
	public Spawn findSpawnById(int id) {
		Spawn spawn = null;
		
		Set<Spawn> spawns = findAllSpawns();
		if( spawns != null && spawns.size() > 0 ) {
			for(Spawn s: spawns) {
				if( id == s.getId() ) {
					spawn = s;
					break;
				}
			}
		}
		
		return spawn;
	}

    public Spawn getNewPlayerSpawn() {
        return findSpawnByName(NEW_PLAYER_SPAWN);
    }

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#getSpawnDefinedGroups()
	 */
	@Override
	public Set<String> getSpawnDefinedGroups() {
		Set<String> definedGroups = new HashSet<String>(5);
		
		Set<Spawn> spawns = findAllSpawns();
		if( spawns != null && spawns.size() > 0 ) {
			for(Spawn s: spawns) {
				if( s.getGroup() != null ) {
					definedGroups.add(s.getGroup());
				}
			}
		}
		
		return definedGroups;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findAllSpawns()
	 */
	/*
	@Override
	public Set<Spawn> findAllSpawns() {
		return super.findAllObjects();
	}
	*/
	
	/** While a YAML file is loading, super.findAllObjects() will fail since
	 * it's not yet loaded. Yet if that YAML file has PlayerSpawn objects in it,
	 * PlayerSpawn has a @ManyToOne mapping to Spawn, so it needs to be able
	 * to find Spawns in order to load. So this temporaryAllObjects works around
	 * the problem by holding Spawn objects as they are loaded so that
	 * PlayerSpawn can find them.
	 */
	private Set<Spawn> temporaryAllObjects;
	@Override
	public Set<Spawn> findAllSpawns() {
		if( StorageYaml.getCurrentlyInitializingInstance() != null ) {
			if( StorageYaml.getCurrentlyInitializingInstance().getSpawnDAO() == this ) {
				return temporaryAllObjects;
			}
		}
		// if there is no intializing going on, then erase the temporary set
		// and fall through to findAllObjects
		else if( temporaryAllObjects != null ) {
			temporaryAllObjects.clear();
			temporaryAllObjects = null;
		}
		
		return super.findAllObjects();
	}
	
	public void spawnLoaded(Spawn spawn) {
		if( StorageYaml.getCurrentlyInitializingInstance() != null ) {
			if( StorageYaml.getCurrentlyInitializingInstance().getSpawnDAO() == this ) {
				if( temporaryAllObjects == null )
					temporaryAllObjects = new HashSet<Spawn>(50);
				
				temporaryAllObjects.add(spawn);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#saveSpawn(org.morganm.homespawnplus.entity.Spawn)
	 */
	@Override
	public void saveSpawn(Spawn spawn) throws StorageException {
		super.saveObject(spawn);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#deleteSpawn(org.morganm.homespawnplus.entity.Spawn)
	 */
	@Override
	public void deleteSpawn(Spawn spawn) throws StorageException {
		super.deleteObject(spawn);
	}

	@Override
	protected SerializableSpawn newSerializable(Spawn object) {
		return new SerializableSpawn(object);
	}

}
