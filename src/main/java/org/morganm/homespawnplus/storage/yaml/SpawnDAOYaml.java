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
	@Override
	public Set<Spawn> findAllSpawns() {
		return super.findAllObjects();
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
