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
/**
 *
 */
package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.commonlib.server.api.Server;
import com.andune.minecraft.commonlib.server.api.event.EventListener;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.config.ConfigWarmup;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Bridge class between Sponge event system and HSP API event interface.
 *
 * @author andune
 */
@Singleton
public class SpongeEventDispatcher implements com.andune.minecraft.commonlib.server.api.event.EventDispatcher {
    private final static Logger log = LoggerFactory.getLogger(SpongeEventDispatcher.class);

    private final EventListener eventListener;
    private final Plugin plugin;
    private final SpongeFactory spongeFactory;
    private final ConfigWarmup configWarmup;
    private final ConfigCore configCore;
    private final Server server;

    @Inject
    public SpongeEventDispatcher(EventListener listener, Plugin plugin, SpongeFactory spongeFactory,
                                 ConfigWarmup configWarmup, ConfigCore configCore, Server server) {
        this.eventListener = listener;
        this.plugin = plugin;
        this.spongeFactory = spongeFactory;
        this.configWarmup = configWarmup;
        this.configCore = configCore;
        this.server = server;
    }

    /**
     * Register events with Sponge
     */
    @Override
    public void registerEvents() {
        // TODO: do something useful for Sponge
    }

}
