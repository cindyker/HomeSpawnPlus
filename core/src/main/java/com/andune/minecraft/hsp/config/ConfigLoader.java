/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.config.ConfigException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * ConfigLoader is responsible for loading config data from storage.
 *
 * @author andune
 */
public interface ConfigLoader {

    /**
     * Load the given configFile and return the configurationSection
     * that represents the config data. This will either load the config
     * from one large file ("config.yml") if it exists or from individual
     * config files.
     *
     * @return
     * @throws ConfigException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public ConfigurationSection load(String fileName, String basePath)
            throws IOException, ConfigException;

    /**
     * ConfigLoader implementation may maintain a cache, especially in the case
     * of a single config.yml file, so when configs are being reloaded, the
     * cache should be flushed by calling this method first.
     */
    public void flush();

}