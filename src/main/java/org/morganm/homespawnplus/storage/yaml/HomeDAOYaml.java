/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.HomeDAO;

/**
 * @author morganm
 *
 */
public class HomeDAOYaml implements HomeDAO {
	private static final String CONFIG_SECTION = "homes";
	
	private YamlConfiguration yaml;
	private File file;
//	private StorageYaml storageYaml;
	private int nextId = -1;
	
	// while we don't do advanced caching here (that can be implemented elsewhere), we
	// do some basic caching and invalidation on update/delete. Since the most common
	// function is reads, this is an easy optimization for the most common use case.
	private boolean cacheInvalid = true;
	private Set<Home> allHomes;
	
	public HomeDAOYaml(final YamlConfiguration yaml, final File file) throws IOException, InvalidConfigurationException {
		this.yaml = yaml;
		this.file = file;
		load();
//		this.storageYaml = storageYaml;
	}
	
	public void load() throws IOException, InvalidConfigurationException {
		yaml.load(file);
	}
	public void save() throws IOException {
		yaml.save(file);
	}
	
	private int getNextId() {
		// if we've already loaded the nextId, just increment it
		if( nextId != -1 )
			return ++nextId;
		
		// otherwise we need to figure it out
		int maxId=0;
		Set<Home> homes = findAllHomes();
		if( homes != null && homes.size() > 0 ) {
			for(Home h: homes) {
				if( h.getId() > maxId )
					maxId = h.getId();
			}
		}
		if( maxId < 1 )
			maxId = 1;
		
		// now that we kow the max ID so far, increment it and return
		nextId = ++maxId;
		return nextId;
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
		
		Set<Home> allHomes = findAllHomes();
		if( allHomes != null && allHomes.size() > 0 ) {
			for(Home h: allHomes) {
				if( playerName.equals(h.getPlayerName()) && world.equals(h.getWorld()) ) {
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

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#findAllHomes()
	 */
	@Override
	public Set<Home> findAllHomes() {
		// if the cache is still valid, just return that
		if( !cacheInvalid )
			return allHomes;
		
		if( allHomes == null )
			allHomes = new HashSet<Home>(100);
		if( cacheInvalid )
			allHomes.clear();
		
		ConfigurationSection section = yaml.getConfigurationSection(CONFIG_SECTION);
		Set<String> keys = section.getKeys(false);
		
		for(String key : keys) {
			SerializableHome sHome = (SerializableHome) yaml.get(CONFIG_SECTION+"."+key);
			allHomes.add(sHome.getHome());
		}
			
		cacheInvalid = false;
		return allHomes;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#saveHome(org.morganm.homespawnplus.entity.Home)
	 */
	@Override
	public void saveHome(Home home) throws StorageException {
		if( home.getId() == 0 )
			home.setId(getNextId());
		
		yaml.set(CONFIG_SECTION+"."+home.getId(), new SerializableHome(home));
		try {
			save();
		}
		catch(Exception e) {
			throw new StorageException(e);
		}

		cacheInvalid = true;
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.HomeDAO#deleteHome(org.morganm.homespawnplus.entity.Home)
	 */
	@Override
	public void deleteHome(Home home) throws StorageException {
		if( home.getId() == 0 )
			return;
		
		yaml.set(CONFIG_SECTION+"."+home.getId(), null);
		try {
			save();
		}
		catch(Exception e) {
			throw new StorageException(e);
		}

		cacheInvalid = true;
	}
	
	static {
		ConfigurationSerialization.registerClass(SerializableHome.class, "Home");
	}
	
	@SerializableAs("Home")
	public static class SerializableHome implements ConfigurationSerializable {
		private final static String ATTR_ID = "id";
		private final static String ATTR_NAME = "name";
		private final static String ATTR_UPDATED_BY = "updatedBy";
		private final static String ATTR_WORLD = "world";
		private final static String ATTR_X = "x";
		private final static String ATTR_Y = "y";
		private final static String ATTR_Z = "z";
		private final static String ATTR_PITCH = "pitch";
		private final static String ATTR_YAW = "yaw";
		private final static String ATTR_DEFAULT_HOME = "defaultHome";
		private final static String ATTR_BED_HOME = "bedHome";
		private final static String ATTR_LAST_MODIFIED = "lastModified";
		private final static String ATTR_DATE_CREATED = "dateCreated";
		
		private Home home;
		public Home getHome() { return home; }
		
		public SerializableHome(Home home) {
			this.home = home;
		}
		
		public SerializableHome(Map<String, Object> map) {
			home = new Home();
			
			Object o = map.get(ATTR_ID);
			if( o instanceof Integer )
				home.setId((Integer) o);
			o = map.get(ATTR_NAME);
			if( o instanceof String )
				home.setName((String) o);
			o = map.get(ATTR_UPDATED_BY);
			if( o instanceof String )
				home.setUpdatedBy((String) o);
			o = map.get(ATTR_WORLD);
			if( o instanceof String )
				home.setWorld((String) o);
			o = map.get(ATTR_X);
			if( o instanceof Double )
				home.setX((Double) o);
			o = map.get(ATTR_Y);
			if( o instanceof Double )
				home.setY((Double) o);
			o = map.get(ATTR_Z);
			if( o instanceof Double )
				home.setZ((Double) o);
			o = map.get(ATTR_PITCH);
			if( o instanceof Double )
				home.setPitch(((Double) o).floatValue());
			o = map.get(ATTR_YAW);
			if( o instanceof Double )
				home.setYaw(((Double) o).floatValue());
			o = map.get(ATTR_BED_HOME);
			if( o instanceof Boolean )
				home.setBedHome((Boolean) o);
			o = map.get(ATTR_DEFAULT_HOME);
			if( o instanceof Boolean )
				home.setDefaultHome((Boolean) o);
			
			o = map.get(ATTR_LAST_MODIFIED);
			if( o instanceof Long )
				home.setLastModified(new Timestamp((Long) o));
			o = map.get(ATTR_DATE_CREATED);
			if( o instanceof Long )
				home.setDateCreated(new Timestamp((Long) o));
		}

		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<String, Object>(15);
			map.put(ATTR_ID, home.getId());
			map.put(ATTR_NAME, home.getName());
			map.put(ATTR_UPDATED_BY, home.getUpdatedBy());
			map.put(ATTR_WORLD, home.getWorld());
			map.put(ATTR_X, home.getX());
			map.put(ATTR_Y, home.getY());
			map.put(ATTR_Z, home.getZ());
			map.put(ATTR_PITCH, home.getPitch());
			map.put(ATTR_YAW, home.getYaw());
			map.put(ATTR_BED_HOME, home.isBedHome());
			map.put(ATTR_DEFAULT_HOME, home.isDefaultHome());
			map.put(ATTR_LAST_MODIFIED, home.getLastModified().getTime());
			map.put(ATTR_DATE_CREATED, home.getDateCreated().getTime());
			return map;
		}
	}
}
