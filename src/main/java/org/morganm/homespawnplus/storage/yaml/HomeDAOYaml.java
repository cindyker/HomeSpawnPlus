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
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHome;

/**
 * @author morganm
 *
 */
public class HomeDAOYaml extends AbstractDAOYaml<Home, SerializableHome> implements HomeDAO {
	private static final String CONFIG_SECTION = "homes";
	
	public HomeDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public HomeDAOYaml(final File file) {
		this(file, null);
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomeById(int)
	 */
	@Override
	public Home findHomeById(int id) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( id == h.getId() ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}
	
	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findDefaultHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home findDefaultHome(String world, String playerName) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( h.isDefaultHome() && playerName.equals(h.getPlayerName()) && world.equals(h.getWorld()) ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findBedHome(java.lang.String, java.lang.String)
	 */
	@Override
	public Home findBedHome(String world, String playerName) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( h.isBedHome() && playerName.equals(h.getPlayerName()) && world.equals(h.getWorld()) ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomeByNameAndPlayer(java.lang.String, java.lang.String)
	 */
	@Override
	public Home findHomeByNameAndPlayer(String homeName, String playerName) {
		Home home = null;
		
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( homeName.equals(h.getName()) && playerName.equals(h.getPlayerName()) ) {
					home = h;
					break;
				}
			}
		}
		
		return home;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomesByWorldAndPlayer(java.lang.String, java.lang.String)
	 */
	@Override
	public Set<Home> findHomesByWorldAndPlayer(String world, String playerName) {
		Set<Home> homes = new HashSet<Home>(4);
		
		if( world == null || "all".equals(world) || "*".equals(world) )
			world = null;
		
		Set<Home> allHomes = findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home h: allHomes) {
				if( playerName.equals(h.getPlayerName()) && (world == null || world.equals(h.getWorld())) ) {
					homes.add(h);
				}
			}
		}
		
		return homes;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findHomesByPlayer(java.lang.String)
	 */
	@Override
	public Set<Home> findHomesByPlayer(String playerName) {
		Set<Home> homes = new HashSet<Home>(4);
		
		Set<Home> allHomes = findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home h: allHomes) {
				if( playerName.equals(h.getPlayerName()) ) {
					homes.add(h);
				}
			}
		}
		
		return homes;
	}

	/** While a YAML file is loading, super.findAllObjects() will fail since
	 * it's not yet loaded. Yet if that YAML file has HomeInvite objects in it,
	 * HomeInvite has a @OneToOne mapping to a Home, so it needs to be able
	 * to find Homes in order to load. So this temporaryAllObjects works around
	 * the problem by holding Home objects as they are loaded so that
	 * HomeInvite can find them.
	 */
	private Set<Home> temporaryAllObjects;
	@Override
	public Set<Home> findAllHomes() {
		if( StorageYaml.getCurrentlyInitializingInstance() != null ) {
			if( StorageYaml.getCurrentlyInitializingInstance().getHomeDAO() == this ) {
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
	
	public void homeLoaded(Home home) {
		if( StorageYaml.getCurrentlyInitializingInstance() != null ) {
			if( StorageYaml.getCurrentlyInitializingInstance().getHomeDAO() == this ) {
				if( temporaryAllObjects == null )
					temporaryAllObjects = new HashSet<Home>(50);
				
				temporaryAllObjects.add(home);
			}
		}
	}
	
	@Override
	public void saveHome(Home home) throws StorageException {
		super.saveObject(home);
	}

	@Override
	public void deleteHome(Home home) throws StorageException {
		super.deleteObject(home);
	}
	
	@Override
	protected SerializableHome newSerializable(Home object) {
		return new SerializableHome(object);
	}
}
