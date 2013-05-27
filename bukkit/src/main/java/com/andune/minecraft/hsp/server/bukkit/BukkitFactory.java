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

import org.bukkit.entity.Player;

import com.andune.minecraft.commonlib.server.api.PermissionSystem;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Command;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.google.inject.Injector;

/**
 * @author andune
 *
 */
@Singleton
public class BukkitFactory extends com.andune.minecraft.commonlib.server.bukkit.BukkitFactory
implements com.andune.minecraft.hsp.server.api.Factory {
    private final ConfigCore configCore;
    private final PlayerDAO playerDAO;
    private final Server server;
    
    @Inject
    protected BukkitFactory(Injector injector, PermissionSystem perm,
            ConfigCore configCore, PlayerDAO playerDAO, Server server) {
        super(injector, perm);
        this.configCore = configCore;
        this.playerDAO = playerDAO;
        this.server = server;
    }

    @Override
    public StrategyContext newStrategyContext() {
        return injector.getInstance(StrategyContext.class);
    }

    @Override
    public Command newCommand(Class<? extends Command> commandClass) {
        return injector.getInstance(commandClass);
    }
    
    @Override
    public BukkitPlayer newBukkitPlayer(Player bukkitPlayer) {
        return new BukkitPlayer(configCore, playerDAO, perm, bukkitPlayer, server);
    }

    @Override
    protected BukkitCommandSender newBukkitCommandSender(org.bukkit.command.CommandSender bukkitSender) {
        return new BukkitCommandSender(bukkitSender, server);
    }
}
