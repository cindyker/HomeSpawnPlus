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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;


import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.CommandSender;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.TeleportOptions;
import com.andune.minecraft.hsp.server.api.YamlFile;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.google.inject.Injector;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitFactory implements Factory {
    private final Injector injector;
    private final ConfigCore configCore;
    private final PlayerDAO playerDAO;
    private final Permissions perm;
    private final Map<String, WeakReference<CommandSender>> senderCache = new HashMap<String, WeakReference<CommandSender>>();
    
    @Inject
    BukkitFactory(Injector injector, ConfigCore configCore, PlayerDAO playerDAO, Permissions perm) {
        this.injector = injector;
        this.configCore = configCore;
        this.playerDAO = playerDAO;
        this.perm = perm;
    }

    @Override
    public Location newLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        return new BukkitLocation(worldName, x, y, z, yaw, pitch);
    }

    @Override
    public TeleportOptions newTeleportOptions() {
        return injector.getInstance(TeleportOptions.class);
    }

    @Override
    public StrategyContext newStrategyContext() {
        return injector.getInstance(StrategyContext.class);
    }

    @Override
    public YamlFile newYamlFile() {
        return injector.getInstance(BukkitYamlConfigFile.class);
    }
    
    public CommandSender getCommandSender(org.bukkit.command.CommandSender bukkitSender) {
        // lookup reference
        WeakReference<CommandSender> ref = senderCache.get(bukkitSender.getName());

        // if reference isn't null, get the object
        CommandSender sender = null;
        if( ref != null )
            sender = ref.get();

        // if object is null, create a new reference
        if( sender == null ) {
            WeakReference<CommandSender> wr = new WeakReference<CommandSender>(new BukkitCommandSender(bukkitSender));
            sender = wr.get();
            senderCache.put(bukkitSender.getName(), wr);
        }

        return sender;
    }
    
    public BukkitPlayer newBukkitPlayer(org.bukkit.entity.Player bukkitPlayer) {
        return new BukkitPlayer(configCore, playerDAO, perm, bukkitPlayer);
    }
}
