/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;

import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;

/** Yaml storage back end.
 * 
 * @author morganm
 *
 */
public class StorageYaml implements Storage {
	private HomeSpawnPlus plugin;
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

	/** Called to write all entities into a single file.
	 * 
	 * @param singleFile if true, the second param is taken to be a single filename that
	 * all YAML data should be written to. If false, the second param is taken to be the
	 * directory that individual YAML files should be stored in.
	 * @param file The file to write to or the directory to put files in, depending on
	 * the value of the first argument.
	 */
	public StorageYaml(final HomeSpawnPlus plugin, final boolean singleFile, final File file) {
		this.plugin = plugin;
		if( singleFile )
			this.singleFile = file;
		else
			this.dataDirectory = file;
	}
	
	@Override
	public void initializeStorage() throws StorageException {
		try {
			if( singleFile != null ) {
				homeDAO = new HomeDAOYaml(singleFile);
				homeInviteDAO = new HomeInviteDAOYaml(plugin, singleFile);
				spawnDAO = new SpawnDAOYaml(singleFile);
				playerDAO = new PlayerDAOYaml(singleFile);
				versionDAO = new VersionDAOYaml(singleFile);
			}
			else {
				File file = new File(dataDirectory, "homes.yml");
				homeDAO = new HomeDAOYaml(file);
				file = new File(dataDirectory, "homeInvites.yml");
				homeInviteDAO = new HomeInviteDAOYaml(plugin, file);
				file = new File(dataDirectory, "spawns.yml");
				spawnDAO = new SpawnDAOYaml(file);
				file = new File(dataDirectory, "players.yml");
				playerDAO = new PlayerDAOYaml(file);
				file = new File(dataDirectory, "version.yml");
				versionDAO = new VersionDAOYaml(file);
			}
			
			allDAOs = new YamlDAOInterface[] { homeDAO, homeInviteDAO, spawnDAO, playerDAO, versionDAO };
		}
		catch(Exception e) {
			throw new StorageException(e);
		}
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
			allDAOs[i].flush();
		}
	}
}
