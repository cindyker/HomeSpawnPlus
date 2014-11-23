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
import com.andune.minecraft.hsp.entity.PlayerLastLocation;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO;
import com.andune.minecraft.hsp.storage.yaml.serialize.SerializablePlayerLastLocation;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class PlayerLastLocationDAOYaml extends AbstractDAOYaml<PlayerLastLocation, SerializablePlayerLastLocation>
        implements PlayerLastLocationDAO {
    private static final String CONFIG_SECTION = "playerLastLocation";

    public PlayerLastLocationDAOYaml(final File file, final YamlConfiguration yaml) {
        super(CONFIG_SECTION);
        this.yaml = yaml;
        this.file = file;
    }

    public PlayerLastLocationDAOYaml(final File file) {
        this(file, null);
    }

    @Override
    public PlayerLastLocation findById(int id) {
        PlayerLastLocation playerLastLocation = null;

        Set<PlayerLastLocation> playerLastLocations = findAll();
        if (playerLastLocations != null && playerLastLocations.size() > 0) {
            for (PlayerLastLocation pll : playerLastLocations) {
                if (id == pll.getId()) {
                    playerLastLocation = pll;
                    break;
                }
            }
        }

        return playerLastLocation;
    }

    @Override
    public PlayerLastLocation findByWorldAndPlayerName(String world, String playerName) {
        PlayerLastLocation playerLastLocation = null;

        Set<PlayerLastLocation> playerLastLocations = findAll();
        if (playerLastLocations != null && playerLastLocations.size() > 0) {
            for (PlayerLastLocation pll : playerLastLocations) {
                if (world.equals(pll.getWorld()) && playerName.equals(pll.getPlayerName())) {
                    playerLastLocation = pll;
                    break;
                }
            }
        }

        return playerLastLocation;
    }

    @Override
    public Set<PlayerLastLocation> findByPlayerName(String playerName) {
        Set<PlayerLastLocation> set = new HashSet<PlayerLastLocation>();

        Set<PlayerLastLocation> playerLastLocations = findAll();
        if (playerLastLocations != null && playerLastLocations.size() > 0) {
            for (PlayerLastLocation pll : playerLastLocations) {
                if (playerName.equals(pll.getPlayerName())) {
                    set.add(pll);
                }
            }
        }

        return set;
    }

    @Override
    public Set<PlayerLastLocation> findAll() {
        return super.findAllObjects();
    }

    @Override
    public void save(PlayerLastLocation playerLastLocation) throws StorageException {
        super.saveObject(playerLastLocation);
    }

    @Override
    protected SerializablePlayerLastLocation newSerializable(PlayerLastLocation object) {
        return new SerializablePlayerLastLocation(object);
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
