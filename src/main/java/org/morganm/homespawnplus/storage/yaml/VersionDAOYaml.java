/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.Version;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.VersionDAO;

/**
 * @author morganm
 *
 */
public class VersionDAOYaml implements VersionDAO, YamlDAOInterface {
	private static final String CONFIG_VERSION = "version";
	private static final int LATEST_VERSION = 150;
	
	private YamlConfiguration yaml;
	private File file;
	private Version version;
	
	public VersionDAOYaml(final File file, final YamlConfiguration yaml) throws IOException, InvalidConfigurationException {
		this.yaml = yaml;
		this.file = file;
		load();
	}
	public VersionDAOYaml(final File file) throws IOException, InvalidConfigurationException {
		this(file, null);
	}
	
	public void load() throws IOException, InvalidConfigurationException {
		this.yaml = new YamlConfiguration();
		if( file.exists() )
			yaml.load(file);
	}
	public void save() throws IOException {
		if( yaml != null ) {
			if( version != null )
				yaml.set(CONFIG_VERSION, version.getVersion());
			
			yaml.save(file);
		}
	}

	@Override
	public Version getVersionObject() {
		if( version == null ) {
			version = new Version();
			version.setId(1);
			version.setVersion(yaml.getInt(CONFIG_VERSION, LATEST_VERSION));
		}
		
		return version;
	}
	
	@Override
	public void invalidateCache() {
		version = null;
	}
	@Override
	public void setDeferredWrite(boolean deferred) {
		// ignored
	}
	@Override
	public void flush() throws StorageException {
		try {
			save();
		} catch(IOException e) {
			throw new StorageException(e);
		}
	}
	
	@Override
	public void deleteAllData() throws StorageException {
		invalidateCache();
		yaml = null;
		if( file != null && file.exists() )
			file.delete();
	}
}
