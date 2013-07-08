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
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import com.andune.minecraft.hsp.storage.dao.HomeInviteDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import com.andune.minecraft.hsp.storage.dao.VersionDAO;

/**
 * Not all DAOs are cached, which is simply a tradeoff between the time it takes
 * to code a cached DAO and the performance gains by doing so. When no cached
 * version of a DAO exists, we just return the backing store's uncached DAO
 * instead.
 * 
 * @author andune
 * 
 */
public class StorageCache implements Storage {
	private final Storage backingStore;
	private final PlayerLastLocationDAOCache playerLastLocationDAO;
	
	public StorageCache(Storage backingStore) {
		this.backingStore = backingStore;
		playerLastLocationDAO = new PlayerLastLocationDAOCache(backingStore.getPlayerLastLocationDAO());
	}

	@Override
	public void initializeStorage() throws StorageException {
		backingStore.initializeStorage();
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
		return backingStore.getSpawnDAO();
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
	 * TODO: at some point, this should force the async writer to flush
	 * synchronously in this calling thread.
	 * 
	 * @throws StorageException
	 */
	@Override
	public void flushAll() throws StorageException {
		backingStore.flushAll();
	}

	@Override
	public String getImplName() {
		return "CACHED_" + backingStore.getImplName();
	}
}
