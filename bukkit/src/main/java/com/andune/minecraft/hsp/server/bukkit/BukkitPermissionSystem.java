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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.plugin.Plugin;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.PermissionSystem;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitPermissionSystem implements PermissionSystem, Initializable {
    private final Plugin plugin;

    private com.andune.minecraft.commonlib.PermissionSystem permSystem;

    @Inject
    public BukkitPermissionSystem(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getSystemInUse() {
        return permSystem.getSystemInUseString();
    }

    @Override
    public boolean has(String worldName, String playerName, String permission) {
        return permSystem.has(worldName, playerName, permission);
    }

    @Override
    public boolean has(CommandSender sender, String permission) {
        return permSystem.has(((BukkitCommandSender) sender).getBukkitSender(), permission);
    }

    @Override
    public void init() throws Exception {
        permSystem = new com.andune.minecraft.commonlib.PermissionSystem(plugin, plugin.getLogger());
        permSystem.setupPermissions();
    }

    @Override
    public void shutdown() throws Exception {
        permSystem = null;
    }

    @Override
    public int getInitPriority() {
        return 6;
    }

    @Override
    public String getPlayerGroup(String playerWorld, String playerName) {
        return permSystem.getPlayerGroup(playerWorld, playerName);
    }
}
