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
package org.morganm.homespawnplus.storage.dao;

import java.util.Set;

import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author morganm
 *
 */
public interface SpawnDAO {
    public static final String NEW_PLAYER_SPAWN = "newPlayerSpawn";

	public Spawn findSpawnByWorld(String world);
	public Spawn findSpawnByWorldAndGroup(String world, String group);
	public Spawn findSpawnByName(String name);
	public Spawn findSpawnById(int id);
	
	/** Return the single spawn defined as new player spawn; if any.
	 * 
	 * @return new player spawn or null
	 */
	public Spawn getNewPlayerSpawn();
	
	/** Return full set of defined spawn groups.
	 * 
	 * @return
	 */
	public Set<String> getSpawnDefinedGroups();

	public Set<Spawn> findAllSpawns();

	public void saveSpawn(Spawn spawn) throws StorageException;
	public void deleteSpawn(Spawn spawn) throws StorageException;
}
