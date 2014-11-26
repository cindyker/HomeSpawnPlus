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
package com.andune.minecraft.hsp.server.bukkit.config;

import com.andune.minecraft.hsp.config.ConfigBootstrap;
import com.andune.minecraft.hsp.config.ConfigOptions;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.storage.BaseStorageFactory;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Singleton;

/**
 * This loads configuration elements as part of "1st pass bootstrap", where
 * config elements actually control parts of the configuration process.
 *
 * A second config pass is done through the normal configuration process of
 * HSP with foreknowledge of the elements loaded during this bootstrap.
 *
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "core.yml", basePath = "core")
public class BukkitConfigBootstrap implements ConfigBootstrap {
    private final YamlConfiguration yaml;

    public BukkitConfigBootstrap(YamlConfiguration yaml) {
        this.yaml = yaml;
    }

    @Override
    public Type getStorageType() {
        return BaseStorageFactory.getType(yaml.getString("core.storage"));
    }

    @Override
    public boolean useInMemoryCache() {
        return yaml.getBoolean("core.inMemoryCache");
    }

    @Override
    public boolean isWarnMissingConfigItems() {
        return yaml.getBoolean("core.warnMissingConfigItems");
    }
}
