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

import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.*;

/**
 * Not all DAOs are cached, which is simply a tradeoff between the time it takes
 * to code a cached DAO and the performance gains by doing so. When no cached
 * version of a DAO exists, we just return the backing store's uncached DAO
 * instead.
 *
 * @author andune
 */
public class StorageCache implements Storage {
    private final Storage backingStore;
    private final PlayerLastLocationDAOCache playerLastLocationDAO;
    private final SpawnDAOCache spawnDAO;
    private final WatchDog watchDog;
    private final AsyncWriter writer;

    public StorageCache(final Storage backingStore) {
        this.backingStore = backingStore;

        watchDog = new WatchDog();
        writer = new AsyncWriter(watchDog);
        playerLastLocationDAO = new PlayerLastLocationDAOCache(backingStore.getPlayerLastLocationDAO(), writer);
        spawnDAO = new SpawnDAOCache(backingStore.getSpawnDAO(), writer);
    }

    @Override
    public void initializeStorage() throws StorageException {
        backingStore.initializeStorage();
        watchDog.start(writer);
    }

    @Override
    public void shutdownStorage() {
        watchDog.shutdown();
        writer.stop();
        writer.flush();
    }

    @Override
    public PlayerLastLocationDAO getPlayerLastLocationDAO() {
        return playerLastLocationDAO;
    }

    @Override
    public HomeDAO getHomeDAO() {
        return backingStore.getHomeDAO();
    }

    @Override
    public HomeInviteDAO getHomeInviteDAO() {
        return backingStore.getHomeInviteDAO();
    }

    @Override
    public SpawnDAO getSpawnDAO() {
        return spawnDAO;
    }

    @Override
    public PlayerDAO getPlayerDAO() {
        return backingStore.getPlayerDAO();
    }

    @Override
    public VersionDAO getVersionDAO() {
        return backingStore.getVersionDAO();
    }

    @Override
    public PlayerSpawnDAO getPlayerSpawnDAO() {
        return backingStore.getPlayerSpawnDAO();
    }

    @Override
    public void purgeCache() {
        playerLastLocationDAO.purgeCache();
        spawnDAO.purgeCache();
        backingStore.purgeCache();
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        int ret = backingStore.purgePlayerData(purgeTime);
        purgeCache();
        return ret;
    }

    @Override
    public int purgeWorldData(String world) {
        int ret = backingStore.purgeWorldData(world);
        purgeCache();
        return ret;
    }

    @Override
    public void deleteAllData() throws StorageException {
        backingStore.deleteAllData();
        purgeCache();
    }

    /**
     * Deferred writes (only used on backup/restore) don't actually change how
     * we manage any caches, so we just pass the hint on to the backing store so
     * it can respond appropriately.
     *
     * @param deferred
     */
    @Override
    public void setDeferredWrites(boolean deferred) {
        backingStore.setDeferredWrites(deferred);
    }

    /**
     * @throws StorageException
     */
    @Override
    public void flushAll() throws StorageException {
        backingStore.flushAll();
        writer.flush();
    }

    @Override
    public String getImplName() {
        return "CACHED_" + backingStore.getImplName();
    }
}
