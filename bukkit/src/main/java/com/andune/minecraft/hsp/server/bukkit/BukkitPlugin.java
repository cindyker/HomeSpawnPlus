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
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.hsp.server.api.Plugin;

/** 
 * 
 * @author andune
 *
 */
@Singleton
public class BukkitPlugin implements Plugin
{
    private final HSPBukkit plugin;
    private final JarUtils jarUtil;
    private String build = null;
    
    @Inject
    public BukkitPlugin(HSPBukkit plugin, JarUtils jarUtil) {
        this.plugin = plugin;
        this.jarUtil = jarUtil;
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public File getJarFile() {
        return plugin._getJarFile();
    }

    @Override
    public String getName() {
        return plugin.getDescription().getName();
    }

    @Override
    public ClassLoader getClassLoader() {
        return plugin._getClassLoader();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getBuild() {
        if( build == null )
            build = jarUtil.getBuild();
        return build;
    }
    
    @Override
    public InputStream getResource(String filename) {
        return plugin.getResource(filename);
    }
}
