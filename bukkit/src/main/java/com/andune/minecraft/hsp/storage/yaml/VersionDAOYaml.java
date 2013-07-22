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

import com.andune.minecraft.hsp.entity.Version;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.VersionDAO;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * @author andune
 */
public class VersionDAOYaml implements VersionDAO, YamlDAOInterface {
    private static final String CONFIG_VERSION = "version";
    private static final int LATEST_VERSION = 150;

    private YamlConfiguration yaml;
    private File file;
    private Version version;

    public VersionDAOYaml(final File file, final YamlConfiguration yaml) {
        this.yaml = yaml;
        this.file = file;
    }

    public VersionDAOYaml(final File file) {
        this(file, null);
    }

    public void load() throws IOException, InvalidConfigurationException {
        this.yaml = new YamlConfiguration();
        if (file.exists())
            yaml.load(file);
    }

    public void save() throws IOException {
        if (yaml != null) {
            if (version != null)
                yaml.set(CONFIG_VERSION, version.getVersion());

            yaml.save(file);
        }
    }

    @Override
    public Version getVersionObject() {
        if (version == null) {
            version = new Version();
            version.setId(1);
            version.setVersion(yaml.getInt(CONFIG_VERSION, LATEST_VERSION));
        }

        return version;
    }

    @Override
    public void invalidateCache() {
        version = null;
    }

    @Override
    public void setDeferredWrite(boolean deferred) {
        // ignored
    }

    @Override
    public void flush() throws StorageException {
        try {
            save();
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteAllData() throws StorageException {
        invalidateCache();
        yaml = null;
        if (file != null && file.exists())
            file.delete();
    }
}
