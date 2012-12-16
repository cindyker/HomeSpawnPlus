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

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.morganm.homespawnplus.OldHSP;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHome;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableHomeInvite;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayer;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayerLastLocation;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayerSpawn;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableSpawn;
import org.morganm.homespawnplus.util.Debug;

/** Yaml storage back end.
 * 
 * @author morganm
 *
 */
public class StorageYaml implements Storage {
	private static StorageYaml currentlyInitializingInstance = null;
	
    // These registrations are required for Bukkit's YAML serialization to work
    static {
        ConfigurationSerialization.registerClass(SerializableHome.class, "Home");
        ConfigurationSerialization.registerClass(SerializableSpawn.class, "Spawn");
        ConfigurationSerialization.registerClass(SerializablePlayer.class, "Player");
        ConfigurationSerialization.registerClass(SerializableHomeInvite.class, "HomeInvite");
        ConfigurationSerialization.registerClass(SerializablePlayerLastLocation.class, "PlayerLastLocation");
        ConfigurationSerialization.registerClass(SerializablePlayerSpawn.class, "PlayerSpawn");
    }

	@SuppressWarnings("unused")
	private final OldHSP plugin;
	private final Debug debug;
	
	// if multiple files are being used, this is the directory they are written to
	private File dataDirectory;
	// if all data is being written into a single file, this points to it
	private File singleFile;
	
	private HomeDAOYaml homeDAO;
	private HomeInviteDAOYaml homeInviteDAO;
	private SpawnDAOYaml spawnDAO;
	private PlayerDAOYaml playerDAO;
	private VersionDAOYaml versionDAO;
	private YamlDAOInterface[] allDAOs;
	private PlayerSpawnDAOYaml playerSpawnDAO;
	private PlayerLastLocationDAOYaml playerLastLocationDAO;

	/**
	 * 
	 * @param singleFile if true, the second param is taken to be a single filename that
	 * all YAML data should be written to. If false, the second param is taken to be the
	 * directory that individual YAML files should be stored in.
	 * @param file The file to write to or the directory to put files in, depending on
	 * the value of the first argument.
	 */
	public StorageYaml(final OldHSP plugin, final boolean singleFile, final File file) {
		this.plugin = plugin;
		this.debug = Debug.getInstance();
		if( singleFile )
			this.singleFile = file;
		else
			this.dataDirectory = file;
	}
	
	public static StorageYaml getCurrentlyInitializingInstance() {
		return currentlyInitializingInstance;
	}
	
	@Override
	public String getImplName() {
		if( singleFile != null )
			return "YAML_SINGLE_FILE";
		else
			return "YAML";
	}

	@Override
	public void initializeStorage() throws StorageException {
		// only allow this to run once per JVM and we keep track of which
		// instance is loading so that while Bukkit is deserializing objects,
		// we can keep track of them for @OneToOne mappings
		synchronized (StorageYaml.class) {
			currentlyInitializingInstance = this;

			// yaml list to load after we're done creating objects
			YamlDAOInterface[] yamlLoadQueue;

			if( singleFile != null ) {
				YamlConfiguration yaml = new YamlConfiguration();
				homeDAO = new HomeDAOYaml(singleFile, yaml);
				homeInviteDAO = new HomeInviteDAOYaml(singleFile, yaml);
				spawnDAO = new SpawnDAOYaml(singleFile, yaml);
				playerDAO = new PlayerDAOYaml(singleFile, yaml);
				versionDAO = new VersionDAOYaml(singleFile, yaml);
				playerSpawnDAO = new PlayerSpawnDAOYaml(singleFile, yaml);
				playerLastLocationDAO = new PlayerLastLocationDAOYaml(singleFile, yaml);

				// only need one load because all DAOs are using the same YamlConfiguration object
				yamlLoadQueue = new YamlDAOInterface[] { homeDAO };
			}
			else {
				File file = new File(dataDirectory, "homes.yml");
				homeDAO = new HomeDAOYaml(file);
				file = new File(dataDirectory, "homeInvites.yml");
				homeInviteDAO = new HomeInviteDAOYaml(file);
				file = new File(dataDirectory, "spawns.yml");
				spawnDAO = new SpawnDAOYaml(file);
				file = new File(dataDirectory, "players.yml");
				playerDAO = new PlayerDAOYaml(file);
				file = new File(dataDirectory, "version.yml");
				versionDAO = new VersionDAOYaml(file);
				file = new File(dataDirectory, "playerSpawns.yml");
				playerSpawnDAO = new PlayerSpawnDAOYaml(file);
				file = new File(dataDirectory, "playerLastLocations.yml");
				playerLastLocationDAO = new PlayerLastLocationDAOYaml(file);

				yamlLoadQueue = new YamlDAOInterface[] { homeDAO, homeInviteDAO, spawnDAO,
						playerDAO, versionDAO, playerSpawnDAO, playerLastLocationDAO };
			}

			allDAOs = new YamlDAOInterface[] { homeDAO, homeInviteDAO, spawnDAO, playerDAO,
					versionDAO, playerSpawnDAO, playerLastLocationDAO };

			// now load the YAML data into memory so it's ready to go when we need it
			for(int i=0; i < yamlLoadQueue.length; i++) {
				try {
					debug.devDebug("calling load on YAML DAO object ",yamlLoadQueue[i]);
					yamlLoadQueue[i].load();
				}
				catch(Exception e) {
					throw new StorageException(e);
				}
			}

			// while loading above, the Bukkit YAML loader will load objects as well.
			// this results in an issue where @OneToOne mappings try to load before
			// the YAML is ready, which in turns results in the cache being loaded
			// as empty. Here we invalidate the cache to get around that issue.
			for(int i=0; i < allDAOs.length; i++) {
				allDAOs[i].invalidateCache();
			}

			currentlyInitializingInstance = null;
		}
	}
	
	/** As homes are loaded, we need to keep track of them because HomeInvite objects
	 * can reference them and the YAML file isn't fully loaded so homeDAO.getAllObjects()
	 * won't work yet.
	 * 
	 * @param home
	 */
	public void homeLoaded(Home home) {
		homeDAO.homeLoaded(home);
	}
	/** Keep track of spawns as they are loading, same as homeLoaded() above.
	 * 
	 * @param home
	 */
	public void spawnLoaded(Spawn spawn) {
		spawnDAO.spawnLoaded(spawn);
	}
	
	@Override
	public org.morganm.homespawnplus.storage.dao.HomeDAO getHomeDAO() { return homeDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.HomeInviteDAO getHomeInviteDAO() { return homeInviteDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.PlayerDAO getPlayerDAO() { return playerDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.SpawnDAO getSpawnDAO() { return spawnDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.VersionDAO getVersionDAO() { return versionDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO getPlayerSpawnDAO() { return playerSpawnDAO; }
	@Override
	public org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO getPlayerLastLocationDAO() { return playerLastLocationDAO; }
	
	@Override
	public void purgeCache() {
		for(int i=0; i < allDAOs.length; i++) {
			allDAOs[i].invalidateCache();
		}
	}

	@Override
	public void deleteAllData() throws StorageException {
		for(int i=0; i < allDAOs.length; i++) {
			allDAOs[i].deleteAllData();
		}
	}

	@Override
	public void setDeferredWrites(boolean deferred) {
		for(int i=0; i < allDAOs.length; i++) {
			allDAOs[i].setDeferredWrite(deferred);
		}
	}

	@Override
	public void flushAll() throws StorageException {
		for(int i=0; i < allDAOs.length; i++) {
			debug.debug("Flushing DAO ",allDAOs[i]);
			allDAOs[i].flush();
		}
	}
}
