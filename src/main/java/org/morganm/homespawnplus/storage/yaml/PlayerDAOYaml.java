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
package org.morganm.homespawnplus.storage.yaml;

import java.io.File;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.morganm.homespawnplus.entity.Player;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.PlayerDAO;
import org.morganm.homespawnplus.storage.yaml.serialize.SerializablePlayer;

/**
 * @author morganm
 *
 */
public class PlayerDAOYaml extends AbstractDAOYaml<Player, SerializablePlayer> implements PlayerDAO {
	private static final String CONFIG_SECTION = "players";

	public PlayerDAOYaml(final File file, final YamlConfiguration yaml) {
		super(CONFIG_SECTION);
		this.yaml = yaml;
		this.file = file;
	}
	public PlayerDAOYaml(final File file) {
		this(file, null);
	}

	@Override
	public Player findPlayerByName(String name) {
		Player player = null;
		
		Set<Player> players = findAllPlayers();
		if( players != null && players.size() > 0 ) {
			for(Player p: players) {
				if( name.equals(p.getName()) ) {
					player = p;
					break;
				}
			}
		}
		
		return player;
	}

	@Override
	public Set<Player> findAllPlayers() {
		return super.findAllObjects();
	}

	@Override
	public void savePlayer(Player player) throws StorageException {
		super.saveObject(player);
	}

	@Override
	protected SerializablePlayer newSerializable(Player object) {
		return new SerializablePlayer(object);
	}
}
