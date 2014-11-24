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
package com.andune.minecraft.hsp.strategies;

import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.hsp.manager.DeathManager;
import com.andune.minecraft.hsp.strategy.*;

import javax.inject.Inject;

/**
 * Allow spawning at a random radius from the last death location.
 *
 * Config file Format:
 *   lastDeathRandomRadius:radius;yVariance
 *
 * yVariance is optional. If you set it to 0, this strategy will try very
 * hard to keep the random spawn location chosen at the same Y level as
 * the original death location, if possible. If you don't set a yVariance,
 * then it is whatever radius you defined. For example, if you set radius
 * at 10, then the yVariance will also be 10, meaning they could spawn 10
 * blocks higher or lower (if a safe spot can be found).
 *
 * Note also this uses HSP's "safe teleport" routines, so it's possible
 * if someone dies in a very small cave that they could be teleported up
 * to the surface, because after a number of random location attempts, the
 * final random location is taken and if it's in a middle of a bunch of
 * brick, the safeTeleport algorithm will move them to the surface.
 *
 * @author andune
 */
@OneArgStrategy
public class LastDeathRandomRadius extends BaseStrategy {
    // max number of times to try a random location that meets the requested
    // admin criteria before giving up and just using the one we have.
    private static final int MAX_TRIES = 10;

    @Inject
    private DeathManager deathManager;
    @Inject
    private Factory factory;
    @Inject
    private Teleport teleport;
    private final String arg;
    private int radius = 0;
    private int yVariance = -1;

    public LastDeathRandomRadius(final String arg) {
        this.arg = arg;
    }

    @Override
    public StrategyResult evaluate(StrategyContext context) {
        final Player p = context.getPlayer();
        final Location l = deathManager.getLastDeathLocation(p);
        log.debug("last death location = {}", l);

        Location result = null;
        if( l != null ) {
            log.debug("radius = {}, yVariance = {}", radius, yVariance);
            final TeleportOptions teleportOptions = context.getTeleportOptions();

            for (int i=0; i < MAX_TRIES; i++) {
                final int minX = l.getBlockX() - radius;
                final int minZ = l.getBlockZ() - radius;
                // if yVariance was given, use that. Otherwise calculate based on radius
                final int minY = yVariance > -1 ? l.getBlockY() - yVariance : l.getBlockY() - radius;
                final Location min = factory.newLocation(l.getWorld().getName(), minX, minY, minZ, 0, 0);
                log.debug("min = {}", min);

                final int maxX = l.getBlockX() + radius;
                final int maxZ = l.getBlockZ() + radius;
                // if yVariance was given, use that. Otherwise calculate based on radius
                final int maxY = yVariance > -1 ? l.getBlockY() - yVariance : l.getBlockY() + radius;
                Location max = factory.newLocation(l.getWorld().getName(), maxX, maxY, maxZ, 0, 0);
                log.debug("max = {}", max);
                result = teleport.findRandomSafeLocation(min, max, teleportOptions);

                // random "safe" result might be outside of admin requested
                // bounds. If so, loop and try again.
                if( yVariance > 0 ) {
                    final int y = result.getBlockY();
                    if( y < minY || y > maxY ) {
                        log.debug("result y={}, minY={}, maxY={}. Retrying random location");
                        continue;
                    }
                }

                // If we get here, we have a good random location, we're done.
                break;
            }
        }

        return resultFactory.create(result);
    }

    @Override
    public void validate() throws StrategyException {
        if (arg == null)
            throw new StrategyException(getStrategyConfigName() + " requires radius as an argument. None was given");

        String radiusString = arg;
        String yVarianceString = null;
        int i = arg.indexOf(";");
        if (i != -1) {
            radiusString = arg.substring(0, i);
            yVarianceString = arg.substring(i + 1, arg.length());
        }

        try {
            radius = Integer.valueOf(radiusString);
        }
        catch (NumberFormatException e) {
            throw new StrategyException(getStrategyConfigName() + " caught exception processing radius argument. A number is required.", e);
        }

        if( yVarianceString != null ) {
            try {
                yVariance = Integer.valueOf(yVarianceString);
            }
            catch (NumberFormatException e) {
                throw new StrategyException(getStrategyConfigName() + " caught exception processing yVariance argument. A number is required.", e);
            }
        }

        if( radius < 1 )
            throw new StrategyException(getStrategyConfigName() + ": radius > 0 is required. Was given "+radius+" instead");
    }
}
