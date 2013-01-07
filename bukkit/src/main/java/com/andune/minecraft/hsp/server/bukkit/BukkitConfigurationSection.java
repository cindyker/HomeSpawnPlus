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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.andune.minecraft.hsp.server.api.ConfigurationSection;

/**
 * @author morganm
 *
 */
public class BukkitConfigurationSection implements ConfigurationSection {
    private static final Set<String> emptyStringSet = Collections.unmodifiableSet(new HashSet<String>());

    private final org.bukkit.configuration.ConfigurationSection bukkitConfigSection;
    
    BukkitConfigurationSection(org.bukkit.configuration.ConfigurationSection bukkitConfigSection) {
        this.bukkitConfigSection = bukkitConfigSection;
    }

    @Override
    public Set<String> getKeys() {
        return bukkitConfigSection.getKeys(false);
    }

    @Override
    public Set<String> getKeys(String path) {
        org.bukkit.configuration.ConfigurationSection section = bukkitConfigSection.getConfigurationSection(path);
        if( section != null ) {
            return section.getKeys(false);
        }
        else
            return emptyStringSet;
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        org.bukkit.configuration.ConfigurationSection section = bukkitConfigSection.getConfigurationSection(path);
        if( section != null )
            return new BukkitConfigurationSection(section);
        else
            return null;
    }

    @Override
    public boolean contains(String path) {
        return bukkitConfigSection.contains(path);
    }

    @Override
    public Object get(String path) {
        return bukkitConfigSection.get(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return bukkitConfigSection.getBoolean(path);
    }

    @Override
    public int getInt(String path) {
        return bukkitConfigSection.getInt(path);
    }

    @Override
    public Integer getInteger(String path) {
        if( bukkitConfigSection.contains(path) )
            return Integer.valueOf(bukkitConfigSection.getInt(path));
        else
            return null;
    }

    @Override
    public String getString(String path) {
        return bukkitConfigSection.getString(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return bukkitConfigSection.getStringList(path);
    }

    @Override
    public double getDouble(String path) {
        return bukkitConfigSection.getDouble(path);
    }
}
