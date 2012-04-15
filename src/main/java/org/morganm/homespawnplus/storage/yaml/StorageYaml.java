/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.util.Debug;

/** Yaml storage back end.
 * 
 * @author morganm
 *
 */
public class StorageYaml implements Storage {
	private static StorageYaml currentlyInitializingInstance = null;
	
	@SuppressWarnings("unused")
	private final HomeSpawnPlus plugin;
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

	/**
	 * 
	 * @param singleFile if true, the second param is taken to be a single filename that
	 * all YAML data should be written to. If false, the second param is taken to be the
	 * directory that individual YAML files should be stored in.
	 * @param file The file to write to or the directory to put files in, depending on
	 * the value of the first argument.
	 */
	public StorageYaml(final HomeSpawnPlus plugin, final boolean singleFile, final File file) {
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

				yamlLoadQueue = new YamlDAOInterface[] { homeDAO, homeInviteDAO, spawnDAO, playerDAO, versionDAO };

			}

			allDAOs = new YamlDAOInterface[] { homeDAO, homeInviteDAO, spawnDAO, playerDAO, versionDAO };

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
	public void objectLoaded(Home home) {
		homeDAO.homeLoaded(home);
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
