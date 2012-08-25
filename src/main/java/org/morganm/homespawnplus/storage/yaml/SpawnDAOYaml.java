/**
 * 
 */
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializableSpawn;

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
