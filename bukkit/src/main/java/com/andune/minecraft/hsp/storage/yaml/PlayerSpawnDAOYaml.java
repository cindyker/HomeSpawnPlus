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
package com.andune.minecraft.hsp.storage.yaml;

import com.andune.minecraft.commonlib.FeatureNotImplemented;
import com.andune.minecraft.hsp.entity.PlayerSpawn;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.storage.yaml.serialize.SerializablePlayerSpawn;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class PlayerSpawnDAOYaml extends AbstractDAOYaml<PlayerSpawn, SerializablePlayerSpawn>
        implements PlayerSpawnDAO {
    private static final String CONFIG_SECTION = "playerSpawns";

    public PlayerSpawnDAOYaml(final File file, final YamlConfiguration yaml) {
        super(CONFIG_SECTION);
        this.yaml = yaml;
        this.file = file;
    }

    public PlayerSpawnDAOYaml(final File file) {
        this(file, null);
    }

    @Override
    public PlayerSpawn findById(int id) {
        PlayerSpawn playerSpawn = null;

        Set<PlayerSpawn> playerSpawns = findAll();
        if (playerSpawns != null && playerSpawns.size() > 0) {
            for (PlayerSpawn s : playerSpawns) {
                if (id == s.getId()) {
                    playerSpawn = s;
                    break;
                }
            }
        }

        return playerSpawn;
    }

    @Override
    public PlayerSpawn findByWorldAndPlayerName(String world, String playerName) {
        PlayerSpawn playerSpawn = null;

        Set<PlayerSpawn> playerSpawns = findAll();
        if (playerSpawns != null && playerSpawns.size() > 0) {
            for (PlayerSpawn s : playerSpawns) {
                if (world.equals(s.getWorld()) && playerName.equals(s.getPlayerName())) {
                    playerSpawn = s;
                    break;
                }
            }
        }

        return playerSpawn;
    }

    @Override
    public Set<PlayerSpawn> findByPlayerName(String playerName) {
        Set<PlayerSpawn> set = new HashSet<PlayerSpawn>();

        Set<PlayerSpawn> playerSpawns = findAll();
        if (playerSpawns != null && playerSpawns.size() > 0) {
            for (PlayerSpawn s : playerSpawns) {
                if (playerName.equals(s.getPlayerName())) {
                    set.add(s);
                }
            }
        }

        return set;
    }

    @Override
    public Set<PlayerSpawn> findAll() {
        return super.findAllObjects();
    }

    @Override
    public void save(PlayerSpawn playerSpawn) throws StorageException {
        super.saveObject(playerSpawn);
    }

    @Override
    public void delete(PlayerSpawn playerSpawn) throws StorageException {
        super.deleteObject(playerSpawn);
    }

    @Override
    protected SerializablePlayerSpawn newSerializable(PlayerSpawn object) {
        return new SerializablePlayerSpawn(object);
    }

    @Override
    public int purgePlayer(String playerName) {
        throw new FeatureNotImplemented();
    }

    @Override
    public Set<String> getAllPlayerNames() {
        throw new FeatureNotImplemented();
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        throw new FeatureNotImplemented();
    }

    @Override
    public int purgeWorldData(String world) {
        throw new FeatureNotImplemented();
    }
}
