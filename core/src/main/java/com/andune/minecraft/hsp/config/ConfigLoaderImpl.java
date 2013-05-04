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
package com.andune.minecraft.hsp.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.Factory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.commonlib.server.api.YamlFile;
import com.andune.minecraft.commonlib.server.api.config.ConfigException;

/**
 * ConfigLoader separated into interface and implementation to avoid
 * circular IoC dependencies.
 * 
 * This implementation will load one large config.yml if it exists or
 * else it will load new-style individual config files instead.
 * 
 * @author andune
 *
 */
@Singleton
public class ConfigLoaderImpl implements ConfigLoader  {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoaderImpl.class);
    
    private final Plugin plugin;
    private final Factory factory;
    private final JarUtils jarUtil;
    
    private YamlFile singleConfigFile;

    @Inject
    public ConfigLoaderImpl(Plugin plugin, Factory factory, JarUtils jarUtil) {
        this.plugin = plugin;
        this.factory = factory;
        this.jarUtil = jarUtil;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.config.ConfigLoader#load(java.lang.String, java.lang.String)
     */
    @Override
    public ConfigurationSection load(String fileName, String basePath) throws IOException, ConfigException {
        YamlFile yaml = getSingleConfigFile();

        // load individual config file if single "config.yml" is not in use
        if( yaml == null ) {
            log.debug("No single config.yml found, using multiple config files");
            File configFileName = new File(plugin.getDataFolder(), "config/"+fileName);
            if( !configFileName.exists() )
                installDefaultFile(fileName);
            yaml = factory.newYamlFile();
            yaml.load(configFileName);
        }

        return yaml.getConfigurationSection(basePath);
    }

    /**
     * If a single config file exists, this method will load it and
     * return it, caching it for future calls. If it doesn't exist,
     * this method returns null.
     * 
     * @return
     * @throws ConfigException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    private YamlFile getSingleConfigFile() throws FileNotFoundException, IOException, ConfigException {
        if( singleConfigFile == null ) {
            File configYml = new File(plugin.getDataFolder(), "config.yml");
            if( configYml.exists() ) {
                log.debug("Single config.yml file exists, loading file");
                singleConfigFile = factory.newYamlFile();
                singleConfigFile.load(configYml);
            }
        }
        
        return singleConfigFile;
    }

    /**
     * Install the configuration default file if it doesn't exist.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void installDefaultFile(String fileName) throws FileNotFoundException, IOException {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if( !configDir.exists() )
            configDir.mkdirs();

        File configFile = new File(configDir, fileName);
        if( !configFile.exists() )
            jarUtil.copyConfigFromJar("config/"+fileName, configFile);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.config.ConfigLoader#flush()
     */
    @Override
    public void flush() {
        singleConfigFile = null;
    }
}
