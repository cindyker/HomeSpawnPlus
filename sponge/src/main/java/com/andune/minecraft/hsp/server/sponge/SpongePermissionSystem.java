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
package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author andune
 *
 */
@Singleton
public class SpongePermissionSystem implements PermissionSystem, Initializable {
    private final Plugin plugin;

    @Inject
    public SpongePermissionSystem(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getSystemInUse() {
        return "Sponge";
    }

    @Override
    public boolean has(String worldName, String playerName, String permission) {
        // TODO: documentation for permissions on Sponge doesn't exist yet, update
        // this to use native Sponge permissions when it exists
        return true;
    }

    @Override
    public boolean has(CommandSender sender, String permission) {
        // TODO: update to use Sponge permissions
        return true;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public int getInitPriority() {
        return 6;
    }

    @Override
    public String getPlayerGroup(String playerWorld, String playerName) {
        // TODO: update to use Sponge permissions
        return "Admin";
    }
}
