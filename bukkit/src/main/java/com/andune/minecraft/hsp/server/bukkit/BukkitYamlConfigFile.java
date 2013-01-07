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
package com.andune.minecraft.hsp.server.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.config.ConfigException;
import com.andune.minecraft.hsp.server.api.YamlFile;

/**
 * @author morganm
 *
 */
public class BukkitYamlConfigFile implements YamlFile {
    private static final Logger log = LoggerFactory.getLogger(BukkitYamlConfigFile.class);

    private final YamlConfiguration yaml;
    private final Plugin plugin;
    
    @Inject
    public BukkitYamlConfigFile(YamlConfiguration yaml, Plugin plugin) {
        this.yaml = yaml;
        this.plugin = plugin;
    }
    
    /**
     * Given a filename, return it's full config file path.
     * @param file
     * @return
     */
//    private File configFile(File file) {
//        File dataDir = plugin.getDataFolder();
//        File configDir = new File(dataDir, "config");
//        return new File(configDir, file.getName());
//    }

    @Override
    public void save(File file) throws IOException {
        yaml.save(file);
    }

    @Override
    public void load(File file) throws FileNotFoundException, IOException, ConfigException {
        log.debug("loading yaml file {}, name = {}", file, file.getName());
        try {
            yaml.load(file);
        }
        catch(InvalidConfigurationException e) {
            throw new ConfigException(e);
        }
        
        // load defaults if possible, if not, ignore any errors
        String fileName = "config/" + file.getName();

        // try file path with prefix, ie. "config/core.yml"
        log.debug("loading defaults for file {}", fileName);
        InputStream defConfigStream = plugin.getResource(fileName);
        if( defConfigStream != null ) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defConfigStream);
            yaml.addDefaults(defaults);
            log.debug("defaults loaded for file {}", file);
        }
        // if that doesn't work, try file path alone, ie. "core.yml"
        else {
            fileName = file.getName();

            // try last two part of file path, ie. "core.yml"
            log.debug("loading defaults for file {}", fileName);
            defConfigStream = plugin.getResource(fileName);
            if( defConfigStream != null ) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(defConfigStream);
                yaml.addDefaults(defaults);
                log.debug("defaults loaded for file {}", file);
            }
        }
    }

    @Override
    public com.andune.minecraft.hsp.server.api.ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection section = yaml.getConfigurationSection(path);
        log.debug("getConfigurationSection() path={}, section={}", path, section);
        if( section != null ) {
            return new BukkitConfigurationSection(section);
        }
        // try defaults
        else {
            ConfigurationSection rootSection = plugin.getConfig().getDefaults();
//            ConfigurationSection rootSection = yaml.getDefaultSection();
            if( rootSection != null )
                section = rootSection.getConfigurationSection(path);
            log.debug("getConfigurationSection() tried defaults, path={}, section={}", path, section);
            if( section != null )
                return new BukkitConfigurationSection(section);
            else
                return null;
        }
    }

    @Override
    public com.andune.minecraft.hsp.server.api.ConfigurationSection getRootConfigurationSection() {
        ConfigurationSection section = yaml.getRoot();
        log.debug("getRootConfigurationSection() section={}", section);
        if( section != null ) {
            return new BukkitConfigurationSection(section);
        }
        // try defaults
        else {
            section = plugin.getConfig().getDefaults();
            log.debug("getConfigurationSection() tried defaults, section={}", section);
            if( section != null )
                return new BukkitConfigurationSection(section);
            else
                return null;
        }
    }
}
