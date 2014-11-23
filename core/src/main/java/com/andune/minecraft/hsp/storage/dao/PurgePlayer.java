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
package com.andune.minecraft.hsp.storage.dao;

import java.util.Set;

/**
 * Interface for DAO storage objects that support the concept of purging a
 * player.
 *
 * @author andune
 */
public interface PurgePlayer {
    /**
     * This method is called to purge a single player from the given backing
     * store for this entity type.
     *
     * @param playerName the playername to purge
     * @return the number of rows purged
     */
    int purgePlayer(String playerName);

    /**
     * Return a Set of all player names that this DAO knows about.
     *
     * @return
     */
    Set<String> getAllPlayerNames();

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.Storage#purgePlayerData(java.lang.Long)
     */
    int purgePlayerData(long purgeTime);

}
