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

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Plugin;
import com.andune.minecraft.hsp.server.api.YamlFile;

/**
 * @author morganm
 *
 */
public class ConfigLoader  {
    private final Plugin plugin;
    private final Factory factory;
    private final JarUtils jarUtil;
    
    private YamlFile singleConfigFile;

    @Inject
    public ConfigLoader(Plugin plugin, Factory factory, JarUtils jarUtil) {
        this.plugin = plugin;
        this.factory = factory;
        this.jarUtil = jarUtil;
    }

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
    public ConfigurationSection load(String fileName, String basePath) throws IOException, ConfigException {
        YamlFile yaml = getSingleConfigFile();

        // load individual config file if single "config.yml" is not in use
        if( yaml == null ) {
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

}
