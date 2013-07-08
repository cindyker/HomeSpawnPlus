/**
 * 
 */
package com.andune.minecraft.hsp.storage.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.andune.minecraft.hsp.entity.PlayerLastLocation;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO;

/**
 * @author andune
 *
 */
public class PlayerLastLocationDAOCache implements PlayerLastLocationDAO {
	private final PlayerLastLocationDAO backingDAO;
	private final Map<Integer, PlayerLastLocation> cacheById;
	private final Map<String, Set<PlayerLastLocation>> cacheByPlayerName;
	private Set<PlayerLastLocation> allObjects;
	
	public PlayerLastLocationDAOCache(final PlayerLastLocationDAO backingStore) {
		this.backingDAO = backingStore;
		cacheById = new HashMap<Integer, PlayerLastLocation>();
		cacheByPlayerName = new HashMap<String, Set<PlayerLastLocation>>();
	}
	
	public void purgeCache() {
		cacheById.clear();
		cacheByPlayerName.clear();
		allObjects = null;
	}

	@Override
	public PlayerLastLocation findById(int id) {
		PlayerLastLocation pll = cacheById.get(id);
		
		// if not cached, then query the backing store
		if( pll == null ) {
			pll = backingDAO.findById(id);
			if( pll != null )
				cacheById.put(id, pll);
		}
		
		return pll;
	}

	/**
	 * We just utilize the playerName cache and filter by the world.
	 */
	@Override
	public PlayerLastLocation findByWorldAndPlayerName(String world, String playerName) {
		Set<PlayerLastLocation> set = findByPlayerName(playerName);
		if( set != null ) {
			for(PlayerLastLocation pll : set) {
				if( pll.getWorld().equals(world) )
					return pll;
			}
		}
		
		return null;
	}

	@Override
	public Set<PlayerLastLocation> findByPlayerName(String playerName) {
		Set<PlayerLastLocation> set = cacheByPlayerName.get(playerName);
		
		// if not cached, then query the backing store
		if( set == null ) {
			set = backingDAO.findByPlayerName(playerName);
			if( set != null )
				cacheByPlayerName.put(playerName, set);
		}
		
		return set;
	}

	@Override
	public Set<PlayerLastLocation> findAll() {
		if( allObjects == null )
			allObjects = backingDAO.findAll();

		return allObjects;
	}

	@Override
	public void save(PlayerLastLocation playerLastLocation) throws StorageException {
		if( allObjects != null )
			allObjects.add(playerLastLocation);
		
		cacheById.put(playerLastLocation.getId(), playerLastLocation);
		
		// only store if the set already exists. If it doesn't exist, the object
		// will just be loaded by the backing store and cached on the next query.
		Set<PlayerLastLocation> set = cacheByPlayerName.get(playerLastLocation.getPlayerName());
		if( set != null )
			set.add(playerLastLocation);
		
		// TODO this needs to use general-purpose async save.
		// For now, for testing, we just pass save through directly
		backingDAO.save(playerLastLocation);
	}

	/**
	 * A purge will invalidate the entire cache. We just invalidate the cache
	 * and then pass the purge through to the backing store.
	 */
	@Override
	public int purgePlayerData(long purgeTime) {
		// TODO: invalidate cache
		return backingDAO.purgePlayerData(purgeTime);
	}

	/**
	 * A purge will invalidate the entire cache. We just invalidate the cache
	 * and then pass the purge through to the backing store.
	 */
	@Override
	public int purgeWorldData(String world) {
		// TODO: invalidate cache
		return backingDAO.purgeWorldData(world);
	}

	/**
	 * A purge will invalidate the entire cache. We just invalidate the cache
	 * and then pass the purge through to the backing store.
	 */
	@Override
	public int purgePlayer(String playerName) {
		// TODO: invalidate cache
		return backingDAO.purgePlayer(playerName);
	}

	/**
	 * This method is only used when purging, so it's use is rare (if ever on
	 * most servers). Therefore no work is done to make this efficient, we just
	 * pass through to the backing store.
	 */
	@Override
	public Set<String> getAllPlayerNames() {
		return backingDAO.getAllPlayerNames();
	}
}
