/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
 */
/**
 *
 */
package com.andune.minecraft.hsp.storage.cache;

import com.andune.minecraft.commonlib.FeatureNotImplemented;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.entity.PlayerLastLocation;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author andune
 */
public class PlayerLastLocationDAOCache implements PlayerLastLocationDAO {
    private final Logger log = LoggerFactory.getLogger(PlayerLastLocationDAOCache.class);
    private final PlayerLastLocationDAO backingDAO;
    private final AsyncWriter asyncWriter;
    private final ConcurrentHashMap<Integer, PlayerLastLocation> cacheById;
    private final Map<String, Set<PlayerLastLocation>> cacheByPlayerName;
    private Set<PlayerLastLocation> allObjects;

    public PlayerLastLocationDAOCache(final PlayerLastLocationDAO backingStore, final AsyncWriter asyncWriter) {
        this.backingDAO = backingStore;
        this.asyncWriter = asyncWriter;
        cacheById = new ConcurrentHashMap<Integer, PlayerLastLocation>();
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
        if (pll == null) {
            pll = backingDAO.findById(id);
            if (pll != null)
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
        if (set != null) {
            for (PlayerLastLocation pll : set) {
                if (pll.getWorld().equals(world))
                    return pll;
            }
        }

        return null;
    }

    @Override
    public Set<PlayerLastLocation> findByPlayerName(String playerName) {
        Set<PlayerLastLocation> set = cacheByPlayerName.get(playerName);

        // if not cached, then query the backing store
        if (set == null) {
            set = backingDAO.findByPlayerName(playerName);
            if (set != null)
                cacheByPlayerName.put(playerName, set);
        }

        return set;
    }

    @Override
    public Set<PlayerLastLocation> findAll() {
        if (allObjects == null)
            allObjects = backingDAO.findAll();

        return allObjects;
    }

    @Override
    public void save(PlayerLastLocation playerLastLocation) throws StorageException {
        if (allObjects != null)
            allObjects.add(playerLastLocation);

        cacheById.put(playerLastLocation.getId(), playerLastLocation);

        // only store if the set already exists. If it doesn't exist, the object
        // will just be loaded by the backing store and cached on the next query.
        Set<PlayerLastLocation> set = cacheByPlayerName.get(playerLastLocation.getPlayerName());
        if (set != null)
            set.add(playerLastLocation);

        asyncWriter.push(new AsyncCommitter(playerLastLocation));
    }

    private class AsyncCommitter implements EntityCommitter {
        private final PlayerLastLocation pll;

        public AsyncCommitter(PlayerLastLocation pll) {
            this.pll = pll;
        }

        public void commit() throws Exception {
            backingDAO.save(pll);
            cacheById.put(pll.getId(), pll);
            log.debug("Saved pll with id {}", pll.getId());
        }
    }

    /*
     * We don't do anything with data purges. They are entirely handled by the
     * StorageCache implementation for us, so these methods are never called. To
     * be sure, we throw an exception that should result in a bug report if they
     * are ever mistakenly called somehow.
     */
    public int purgePlayerData(long purgeTime) {
        throw new FeatureNotImplemented();
    }

    public int purgeWorldData(String world) {
        throw new FeatureNotImplemented();
    }

    public int purgePlayer(String playerName) {
        throw new FeatureNotImplemented();
    }

    public Set<String> getAllPlayerNames() {
        throw new FeatureNotImplemented();
    }
}
