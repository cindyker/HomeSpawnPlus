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

import com.andune.minecraft.commonlib.server.api.Scheduler;
import org.spongepowered.api.Game;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * @author andune
 *
 */
@Singleton
public class SpongeScheduler implements Scheduler {
    private Game game;
    private SpongePlugin plugin;

    @Inject
    public SpongeScheduler(SpongePlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    // TOOD: fix return value
    @Override
    public int scheduleSyncDelayedTask(Runnable task, long delay) {
        game.getSyncScheduler().runTaskAfter(plugin.getPluginObject(), task, delay);
        return -1;
    }

    // TOOD: fix return value
    @Override
    public int scheduleAsyncDelayedTask(Runnable task, long delay) {
        game.getAsyncScheduler().runTaskAfter(plugin.getPluginObject(), task, TimeUnit.MILLISECONDS, delay);
        return -1;
    }

}
