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

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;

/** Abstract base class that implements some common functionality
 * for config classes.
 * 
 * @author andune
 *
 */
public abstract class ConfigBase implements Initializable {
    protected static final Logger log = LoggerFactory.getLogger(ConfigBase.class);
    
    @Inject private ConfigLoader configLoader; 
    protected ConfigurationSection configSection;

    @Override
    public void init() throws Exception {
        ConfigOptions configOptions = getClass().getAnnotation(ConfigOptions.class);
        if( configOptions == null )
            throw new ConfigException("Annotation @ConfigOptions missing from class "+getClass());

        configSection = configLoader.load(configOptions.fileName(), configOptions.basePath());
        log.debug("configSection = {}", configSection);
    }
    
    protected String getBasePath() {
        return getClass().getAnnotation(ConfigOptions.class).basePath();
    }
    
    @Override
    public void shutdown() throws Exception {};
    
    @Override
    public int getInitPriority() {
        return 3;   // default config initialization priority is 3
    }

    protected boolean contains(String path) {
        return configSection.contains(path);
    }
    protected Object get(String path) {
        return configSection.get(path);
    }
    protected boolean getBoolean(String path) {
        return configSection.getBoolean(path);
    }
    protected int getInt(String path) {
        return configSection.getInt(path);
    }
    protected Integer getInteger(String path) {
        return configSection.getInteger(path);
    }
    protected double getDouble(String path) {
        return configSection.getDouble(path);
    }
    protected String getString(String path) {
        return configSection.getString(path);
    }
    protected Set<String> getKeys(String path) {
        return configSection.getKeys(path);
    }
    protected List<String> getStringList(String path) {
        return configSection.getStringList(path);
    }
}
