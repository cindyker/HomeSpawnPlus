/**
 * 
 */
package com.andune.minecraft.hsp.storage.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.andune.minecraft.commonlib.FeatureNotImplemented;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;

/**
 *
 * @author andune
 *
 */
public class SpawnDAOCache implements SpawnDAO {
    private final Logger log = LoggerFactory.getLogger(SpawnDAOCache.class);
    private final SpawnDAO backingDAO;
    private final AsyncWriter asyncWriter;
    private final ConcurrentHashMap<Integer, Spawn> cacheById;
    private final Map<String, Spawn> cacheByName;
    private final Map<String, Map<String, Spawn>> cacheByWorldAndGroup;
    private Set<? extends Spawn> allObjects;

    public SpawnDAOCache(final SpawnDAO backingStore, final AsyncWriter asyncWriter) {
        this.backingDAO = backingStore;
        this.asyncWriter = asyncWriter;
        
        cacheById = new ConcurrentHashMap<Integer, Spawn>();
        cacheByName = new HashMap<String, Spawn>();
        cacheByWorldAndGroup = new HashMap<String, Map<String, Spawn>>();
    }

    public void purgeCache() {
        cacheById.clear();
        cacheByWorldAndGroup.clear();
        cacheByName.clear();
        allObjects = null;
    }

    @Override
    public Spawn findSpawnByWorld(String world) {
        return findSpawnByWorldAndGroup(world, Storage.HSP_WORLD_SPAWN_GROUP);
    }

    @Override
    public Spawn findSpawnByWorldAndGroup(String world, String group) {
        Map<String, Spawn> worldMap = cacheByWorldAndGroup.get(world);
        if( worldMap == null ) {
            worldMap = new HashMap<String, Spawn>();
            cacheByWorldAndGroup.put(world, worldMap);
        }
        
        Spawn spawn = worldMap.get(group);
        if( spawn == null ) {
            spawn = backingDAO.findSpawnByWorldAndGroup(world, group);
            if( spawn != null )
                worldMap.put(group, spawn);
        }
        
        return spawn;
    }

    @Override
    public Spawn findSpawnByName(String name) {
        Spawn spawn = cacheByName.get(name);
        
        // if not cached, then query the backing store
        if( spawn == null ) {
            spawn = backingDAO.findSpawnByName(name);
            if( spawn != null )
                cacheByName.put(name, spawn);
        }
        
        return spawn;
    }

   @Override
    public Spawn findSpawnById(int id) {
        Spawn spawn = cacheById.get(id);
        
        // if not cached, then query the backing store
        if( spawn == null ) {
            spawn = backingDAO.findSpawnById(id);
            if( spawn != null )
                cacheById.put(id, spawn);
        }
        
        return spawn;
    }

    @Override
    public Spawn getNewPlayerSpawn() {
        return findSpawnByName(NEW_PLAYER_SPAWN);
    }

    /**
     * DRY violation: same algorithm exists in SpawnDAOEbean.
     */
    @Override
    public Set<String> getSpawnDefinedGroups() {
        Set<String> groups = new HashSet<String>();
        Set<? extends Spawn> spawns = findAllSpawns();
        
        for(Spawn spawn : spawns) {
            String group = spawn.getGroup();
            if( group != null )
                groups.add(group);
        }
        
        return groups;
    }

    @Override
    public Set<? extends Spawn> findAllSpawns() {
        if( allObjects == null )
            allObjects = backingDAO.findAllSpawns();

        return allObjects;
    }

    @Override
    public void saveSpawn(Spawn spawn) throws StorageException {
        asyncWriter.push(new AsyncCommitter(spawn, false));

        try {
            if( spawn.getName() != null )
                cacheByName.put(spawn.getName(), spawn);
            if( spawn.getGroup() != null ) {
                Map<String, Spawn> worldMap = cacheByWorldAndGroup.get(spawn.getWorld());
                if( worldMap == null ) {
                    worldMap = new HashMap<String, Spawn>();
                    cacheByWorldAndGroup.put(spawn.getWorld(), worldMap);
                }
                
                worldMap.put(spawn.getGroup(), spawn);
            }
        }
        catch(Exception e) {
            log.warn("Caught exception in saveSpawn. Please report this issue to the developer.", e);
            purgeCache();   // safety mechanism, if we have a failure, we purge the cache
        }
    }

    @Override
    public void deleteSpawn(Spawn spawn) throws StorageException {
        asyncWriter.push(new AsyncCommitter(spawn, true));

        // remove from various caches
        try {
            cacheById.remove(spawn.getId());
            cacheByName.remove(spawn.getName());
            Map<String, Spawn> worldMap = cacheByWorldAndGroup.get(spawn.getWorld());
            if( worldMap != null )
                worldMap.remove(spawn.getGroup());
        }
        catch(Exception e) {
            log.warn("Caught exception in deleteSpawn. Please report this issue to the developer.", e);
            purgeCache();   // safety mechanism, if we have a failure, we purge the cache
        }
    }

    private class AsyncCommitter implements EntityCommitter {
        private final Spawn spawn;
        private final boolean isDelete;
        public AsyncCommitter(Spawn spawn, boolean isDelete) {
            this.spawn = spawn;
            this.isDelete = isDelete;
        }

        public void commit() throws Exception {
            if( isDelete )
                backingDAO.deleteSpawn(spawn);
            else {
                backingDAO.saveSpawn(spawn);
                cacheById.put(spawn.getId(), spawn);
                log.debug("Saved spawn with id {}", spawn.getId());
            }
        }
    }

    /*
     * We don't do anything with data purges. They are entirely handled by the
     * StorageCache implementation for us, so these methods are never called. To
     * be sure, we throw an exception that should result in a bug report if they
     * are ever mistakenly called somehow.
     */
    public int purgeWorldData(String world) { throw new FeatureNotImplemented(); }
}
