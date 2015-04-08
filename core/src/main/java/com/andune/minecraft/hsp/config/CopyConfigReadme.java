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

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.JarUtils;
import com.andune.minecraft.commonlib.server.api.Plugin;

import javax.inject.Inject;
import java.io.File;

/**
 * Simple class that copies the config/README.txt file into place from the
 * plugin JAR file.
 *
 * @author andune
 */
public class CopyConfigReadme implements Initializable {
    private final String README_FILE = "README.txt";

    private final Plugin plugin;
    private final JarUtils jarUtil;

    @Inject
    public CopyConfigReadme(Plugin plugin, JarUtils jarUtil) {
        this.plugin = plugin;
        this.jarUtil = jarUtil;
    }

    @Override
    public void init() throws Exception {
        File pluginDir = plugin.getDataFolder();
        // create the config directory if it doesn't exist
        File configDir = new File(pluginDir, "config");
        if (!configDir.exists())
            configDir.mkdirs();

        File readmeFile = new File(configDir, README_FILE);
        if (!readmeFile.exists())
            jarUtil.copyConfigFromJar("config/" + README_FILE, readmeFile);
    }

    @Override
    public void shutdown() throws Exception {
        // do nothing
    }

    @Override
    public int getInitPriority() {
        return 9;
    }
}
