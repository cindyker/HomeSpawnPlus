/**
 * 
 */
package org.morganm.homespawnplus.storage.ebean;

import java.util.HashSet;
import java.util.Set;

import org.morganm.homespawnplus.entity.Spawn;
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
		
		Query<Spawn> query = ebean.createQuery(Spawn.class, q);
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
		
		Query<Spawn> query = ebean.createQuery(Spawn.class, q);
		query.setParameter("name", name);
		
		return query.findUnique();
	}

	/* (non-Javadoc)
	 * @see org.morganm.homespawnplus.storage.dao.SpawnDAO#findSpawnById(int)
	 */
	@Override
	public Spawn findSpawnById(int id) {
		String q = "find spawn where id = :id";
		
		Query<Spawn> query = ebean.createQuery(Spawn.class, q);
		query.setParameter("id", id);
		
		return query.findUnique();
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
		Set<Spawn> spawns = findAllSpawns();
		
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
	public Set<Spawn> findAllSpawns() {
		return ebean.find(Spawn.class).findSet();
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
