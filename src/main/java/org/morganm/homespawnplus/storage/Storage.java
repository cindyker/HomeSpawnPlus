/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
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
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.morganm.homespawnplus.storage;

import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.storage.dao.HomeInviteDAO;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.morganm.homespawnplus.storage.dao.PlayerLastLocationDAO;
import org.morganm.homespawnplus.storage.dao.PlayerSpawnDAO;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;
import org.morganm.homespawnplus.storage.dao.UUIDHistoryDAO;
import org.morganm.homespawnplus.storage.dao.VersionDAO;


/** Storage interface for stored objects this plugin uses.
 * 
 * @author morganm
 *
 */
public interface Storage {
	public static final String HSP_WORLD_SPAWN_GROUP = "HSP_GLOBAL";
	public static final String HSP_BED_RESERVED_NAME = "bed";
	
	/* This method is called to initialize the storage system.  If using a DB back end, this
	 * is the method that should create the tables if they don't exist.
	 * 
	 * It is possible that this method could be called multiple times, so it is this methods
	 * responsibility to keep track of whether it has already initialized and deal with that
	 * situation appropriately. 
	 */
	public void initializeStorage() throws StorageException;
	
	public HomeDAO getHomeDAO();
	public HomeInviteDAO getHomeInviteDAO();
	public SpawnDAO getSpawnDAO();
	public PlayerDAO getPlayerDAO();
	public VersionDAO getVersionDAO();
	public PlayerSpawnDAO getPlayerSpawnDAO();
	public PlayerLastLocationDAO getPlayerLastLocationDAO();
    public UUIDHistoryDAO getUUIDHistoryDAO();

	/** Notify the backing store that it should purge any in-memory cache it has.
	 */
	public void purgeCache();
	
	public void deleteAllData() throws StorageException;
	
	/** Optional implementation: the backing store can use this to respond to applications
	 * wish to defer writes, as often happens with bulk loading or perhaps if the application
	 * wants to flush writes on a timed cycle. Storage backends are not required to do
	 * anything at all with this, it is just a hint.
	 * 
	 * @param deferred
	 */
	public void setDeferredWrites(boolean deferred);
	/** For use with setDeferredWrites() above, this method instructs the backend that now
	 * is a good time to flush any pending writes to storage. Again, a completely optional
	 * implementation for the storage system, so there is no guarantee calling this does
	 * anything. This is just a hint to the back-end storage that now is a good time to
	 * flush pending writes.
	 */
	public void flushAll() throws StorageException;
	
	/** Return a String name for this storage implementation, which can be used for
	 * metrics purposes.
	 * 
	 * @return
	 */
	public String getImplName();
}
