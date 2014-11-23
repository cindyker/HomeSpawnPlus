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

import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.commonlib.server.api.Factory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.commonlib.server.api.YamlFile;
import com.andune.minecraft.commonlib.server.api.config.ConfigException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * ConfigLoader separated into interface and implementation to avoid
 * circular IoC dependencies.
 * <p/>
 * This implementation will load one large config.yml if it exists or
 * else it will load new-style individual config files instead.
 *
 * @author andune
 */
@Singleton
public class ConfigLoaderImpl implements ConfigLoader {
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
        // load individual config file. This will either be used directly, or
        // if a single large config file is found, this will be used as defaults
        YamlFile singleYaml = loadSingleConfig(fileName, basePath);

        YamlFile yaml = getSingleConfigFile();
        boolean singleConfigFlag = false;
        if (yaml == null) {
            log.debug("No single config.yml found, using multiple config files");
            yaml = singleYaml;
        }
        else {
            singleConfigFlag = true;

            if( singleYaml != null ) {
                log.debug("Single YAML file in use. Applying defaults for {} from 2.0-style config.", fileName);
                yaml.addDefaultConfig(singleYaml.getRootConfigurationSection());
            }
        }

        ConfigurationSection cs = yaml.getConfigurationSection(basePath);

        // returning null config is bad, we need to do something about it
        if (cs == null) {
            // if we're using single config file (probably old config), then
            // try to load the defaults out of new-style config
            if (yaml != null) {
                log.debug("Defaults for \"{}\" missing, trying to load from single config defaults", basePath);
                yaml = loadSingleConfig(fileName, basePath);
                cs = yaml.getConfigurationSection(basePath);
            }

            // if it's still null, create an empty section and print a warning
            if (cs == null) {
                cs = yaml.createConfigurationSection(basePath);
                log.warn("ConfigurationSection \"" + basePath + "\" not found, bad things might happen!");
            }
        }

        return cs;
    }

    private YamlFile loadSingleConfig(String fileName, String basePath) throws IOException, ConfigException {
        File configFileName = new File(plugin.getDataFolder(), "config/" + fileName);
        if (!configFileName.exists())
            installDefaultFile(fileName);
        YamlFile yaml = factory.newYamlFile();
        yaml.load(configFileName);
        return yaml;
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
    private YamlFile getSingleConfigFile() throws IOException, ConfigException {
        if (singleConfigFile == null) {
            File configYml = new File(plugin.getDataFolder(), "config.yml");
            if (configYml.exists()) {
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
    private void installDefaultFile(String fileName) throws IOException {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if (!configDir.exists())
            configDir.mkdirs();

        File configFile = new File(configDir, fileName);
        if (!configFile.exists())
            jarUtil.copyConfigFromJar("config/" + fileName, configFile);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.config.ConfigLoader#flush()
     */
    @Override
    public void flush() {
        singleConfigFile = null;
    }
}
